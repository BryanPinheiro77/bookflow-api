package br.com.bookflow.emprestimo.service;

import br.com.bookflow.emprestimo.dto.CriarEmprestimoRequest;
import br.com.bookflow.emprestimo.dto.EmprestimoResponse;
import br.com.bookflow.emprestimo.entity.Emprestimo;
import br.com.bookflow.emprestimo.entity.EmprestimoStatus;
import br.com.bookflow.emprestimo.repository.EmprestimoRepository;
import br.com.bookflow.exception.PermissaoNegadaException;
import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.exception.RegraDeNegocioException;
import br.com.bookflow.interesse.entity.InteresseLivro;
import br.com.bookflow.interesse.repository.InteresseLivroRepository;
import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.livro.entity.LivroStatus;
import br.com.bookflow.livro.repository.LivroRepository;
import br.com.bookflow.notificacao.service.NotificacaoService;
import br.com.bookflow.usuario.entity.Role;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private InteresseLivroRepository interesseLivroRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private EmprestimoService emprestimoService;

    @Test
    void deveCriarEmprestimoComSucesso() {

        Long usuarioId = 1L;
        Long livroId = 2L;

        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .nome("Bryan")
                .email("bryan@email.com")
                .role(Role.USUARIO)
                .build();

        Usuario admin = Usuario.builder()
                .id(10L)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(livroId)
                .titulo("Clean Code")
                .quantidadeTotal(10)
                .quantidadeDisponivel(5)
                .valorEmprestimo(new BigDecimal("12.90"))
                .valorMultaDiaria(new BigDecimal("1.50"))
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        CriarEmprestimoRequest request =
                new CriarEmprestimoRequest(livroId);

        when(usuarioRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        when(livroRepository.findById(livroId))
                .thenReturn(Optional.of(livro));

        when(emprestimoRepository
                .existsByUsuarioIdAndLivroTituloAndStatus(
                        anyLong(),
                        anyString(),
                        any()
                ))
                .thenReturn(false);

        when(emprestimoRepository
                .countByUsuarioIdAndLivroAdminIdAndStatus(
                        anyLong(),
                        anyLong(),
                        any()
                ))
                .thenReturn(0L);

        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(invocation -> {

                    Emprestimo e = invocation.getArgument(0);
                    e.setId(100L);

                    return e;
                });

        EmprestimoResponse response =
                emprestimoService.criar(request, usuarioId);

        assertNotNull(response);

        assertEquals(100L, response.id());
        assertEquals(EmprestimoStatus.ATIVO, response.status());

        assertEquals(
                "Clean Code",
                response.tituloLivro()
        );

        assertEquals(
                4,
                livro.getQuantidadeDisponivel()
        );

        verify(notificacaoService).criarNotificacao(
                eq(admin),
                eq("Novo empréstimo realizado"),
                eq("O livro \"Clean Code\" foi emprestado para Bryan.")
        );

        verify(livroRepository).save(livro);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistirAoCriarEmprestimo() {

        CriarEmprestimoRequest request =
                new CriarEmprestimoRequest(2L);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception =
                assertThrows(
                        RecursoNaoEncontradoException.class,
                        () -> emprestimoService.criar(request, 1L)
                );

        assertEquals(
                "Usuário não encontrado.",
                exception.getMessage()
        );
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoExistirAoCriarEmprestimo() {

        Usuario usuario = Usuario.builder()
                .id(1L)
                .role(Role.USUARIO)
                .build();

        CriarEmprestimoRequest request =
                new CriarEmprestimoRequest(2L);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(livroRepository.findById(2L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception =
                assertThrows(
                        RecursoNaoEncontradoException.class,
                        () -> emprestimoService.criar(request, 1L)
                );

        assertEquals(
                "Livro não encontrado.",
                exception.getMessage()
        );
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverQuantidadeDisponivel() {

        Usuario usuario = Usuario.builder()
                .id(1L)
                .role(Role.USUARIO)
                .build();

        Usuario admin = Usuario.builder()
                .id(10L)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .quantidadeTotal(5)
                .quantidadeDisponivel(0)
                .status(LivroStatus.INDISPONIVEL)
                .admin(admin)
                .build();

        CriarEmprestimoRequest request =
                new CriarEmprestimoRequest(2L);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(livroRepository.findById(2L))
                .thenReturn(Optional.of(livro));

        RegraDeNegocioException exception =
                assertThrows(
                        RegraDeNegocioException.class,
                        () -> emprestimoService.criar(request, 1L)
                );

        assertEquals(
                "Não há exemplares disponíveis para este livro.",
                exception.getMessage()
        );
    }

    @Test
    void deveImpedirEmprestimoDeMesmoLivro() {

        Long usuarioId = 1L;
        Long livroId = 2L;

        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .role(Role.USUARIO)
                .build();

        Usuario admin = Usuario.builder()
                .id(10L)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(livroId)
                .titulo("Clean Code")
                .quantidadeDisponivel(5)
                .admin(admin)
                .build();

        when(usuarioRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        when(livroRepository.findById(livroId))
                .thenReturn(Optional.of(livro));

        when(emprestimoRepository
                .existsByUsuarioIdAndLivroTituloAndStatus(
                        usuarioId,
                        "Clean Code",
                        EmprestimoStatus.ATIVO
                ))
                .thenReturn(true);

        RegraDeNegocioException exception =
                assertThrows(
                        RegraDeNegocioException.class,
                        () -> emprestimoService.criar(
                                new CriarEmprestimoRequest(livroId),
                                usuarioId
                        )
                );

        assertEquals(
                "Você já possui este livro emprestado.",
                exception.getMessage()
        );
    }

    @Test
    void deveImpedirMaisDeTresEmprestimosNaMesmaBiblioteca() {

        Long usuarioId = 1L;
        Long livroId = 2L;

        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .role(Role.USUARIO)
                .build();

        Usuario admin = Usuario.builder()
                .id(10L)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(livroId)
                .titulo("Livro")
                .quantidadeDisponivel(5)
                .admin(admin)
                .build();

        when(usuarioRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        when(livroRepository.findById(livroId))
                .thenReturn(Optional.of(livro));

        when(emprestimoRepository
                .existsByUsuarioIdAndLivroTituloAndStatus(
                        anyLong(),
                        anyString(),
                        any()
                ))
                .thenReturn(false);

        when(emprestimoRepository
                .countByUsuarioIdAndLivroAdminIdAndStatus(
                        usuarioId,
                        admin.getId(),
                        EmprestimoStatus.ATIVO
                ))
                .thenReturn(3L);

        RegraDeNegocioException exception =
                assertThrows(
                        RegraDeNegocioException.class,
                        () -> emprestimoService.criar(
                                new CriarEmprestimoRequest(livroId),
                                usuarioId
                        )
                );

        assertEquals(
                "Você atingiu o limite de empréstimos desta biblioteca.",
                exception.getMessage()
        );
    }

    @Test
    void deveDevolverEmprestimoComSucessoSemInteressados() {

        Long adminId = 10L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .role(Role.ADMIN)
                .build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .role(Role.USUARIO)
                .build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .quantidadeTotal(5)
                .quantidadeDisponivel(0)
                .status(LivroStatus.INDISPONIVEL)
                .admin(admin)
                .build();

        Emprestimo emprestimo = Emprestimo.builder()
                .id(100L)
                .usuario(usuario)
                .livro(livro)
                .dataEmprestimo(LocalDate.now().minusDays(3))
                .dataPrevistaDevolucao(LocalDate.now().plusDays(27))
                .status(EmprestimoStatus.ATIVO)
                .build();

        when(emprestimoRepository.findById(100L))
                .thenReturn(Optional.of(emprestimo));

        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenReturn(emprestimo);

        when(interesseLivroRepository
                .findByLivroIdOrderByDataInteresseAsc(
                        livro.getId()
                ))
                .thenReturn(List.of());

        EmprestimoResponse response =
                emprestimoService.devolver(100L, adminId);

        assertEquals(
                EmprestimoStatus.FINALIZADO,
                response.status()
        );

        assertEquals(
                LivroStatus.DISPONIVEL,
                livro.getStatus()
        );

        assertEquals(
                1,
                livro.getQuantidadeDisponivel()
        );

        assertNotNull(response.dataDevolucao());

        verify(livroRepository).save(livro);

        verify(notificacaoService, never())
                .criarNotificacao(any(), anyString(), anyString());
    }

    @Test
    void deveCalcularMultaCorretamente() {

        Usuario admin = Usuario.builder()
                .id(10L)
                .role(Role.ADMIN)
                .build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .role(Role.USUARIO)
                .build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .valorMultaDiaria(new BigDecimal("2.00"))
                .admin(admin)
                .build();

        Emprestimo emprestimo = Emprestimo.builder()
                .id(100L)
                .usuario(usuario)
                .livro(livro)
                .dataEmprestimo(LocalDate.now().minusDays(40))
                .dataPrevistaDevolucao(LocalDate.now().minusDays(10))
                .status(EmprestimoStatus.ATIVO)
                .build();

        when(emprestimoRepository.findByUsuarioId(1L))
                .thenReturn(List.of(emprestimo));

        List<EmprestimoResponse> response =
                emprestimoService.listarPorUsuario(1L);

        assertEquals(
                10,
                response.get(0).diasAtraso()
        );

        assertEquals(
                new BigDecimal("20.00"),
                response.get(0).multaAtual()
        );
    }

    @Test
    void deveLancarExcecaoAoDevolverEmprestimoDeOutroAdmin() {

        Long adminLogadoId = 10L;
        Long adminDonoLivroId = 20L;

        Usuario adminDono = Usuario.builder()
                .id(adminDonoLivroId)
                .role(Role.ADMIN)
                .build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .role(Role.USUARIO)
                .build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .admin(adminDono)
                .build();

        Emprestimo emprestimo = Emprestimo.builder()
                .id(100L)
                .usuario(usuario)
                .livro(livro)
                .status(EmprestimoStatus.ATIVO)
                .build();

        when(emprestimoRepository.findById(100L))
                .thenReturn(Optional.of(emprestimo));

        PermissaoNegadaException exception =
                assertThrows(
                        PermissaoNegadaException.class,
                        () -> emprestimoService.devolver(
                                100L,
                                adminLogadoId
                        )
                );

        assertEquals(
                "Você não tem permissão para devolver este empréstimo.",
                exception.getMessage()
        );
    }
}