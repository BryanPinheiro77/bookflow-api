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

        Usuario usuario = Usuario.builder().id(usuarioId).role(Role.USUARIO).build();
        Usuario admin = Usuario.builder().id(10L).role(Role.ADMIN).build();

        Livro livro = Livro.builder()
                .id(livroId)
                .titulo("Dom Casmurro")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        CriarEmprestimoRequest request = new CriarEmprestimoRequest(livroId);

        Emprestimo emprestimoSalvo = Emprestimo.builder()
                .id(100L)
                .usuario(usuario)
                .livro(livro)
                .dataEmprestimo(LocalDate.now())
                .status(EmprestimoStatus.ATIVO)
                .build();

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(livroId)).thenReturn(Optional.of(livro));
        when(emprestimoRepository.existsByLivroIdAndStatus(livroId, EmprestimoStatus.ATIVO)).thenReturn(false);
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimoSalvo);

        EmprestimoResponse response = emprestimoService.criar(request, usuarioId);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(EmprestimoStatus.ATIVO, response.status());
        assertEquals("Dom Casmurro", response.tituloLivro());
        assertEquals(LivroStatus.EMPRESTADO, livro.getStatus());

        verify(livroRepository).save(livro);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistirAoCriarEmprestimo() {
        CriarEmprestimoRequest request = new CriarEmprestimoRequest(2L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> emprestimoService.criar(request, 1L)
        );

        assertEquals("Usuário não encontrado.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoExistirAoCriarEmprestimo() {
        Usuario usuario = Usuario.builder().id(1L).role(Role.USUARIO).build();
        CriarEmprestimoRequest request = new CriarEmprestimoRequest(2L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(2L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> emprestimoService.criar(request, 1L)
        );

        assertEquals("Livro não encontrado.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoEstiverDisponivel() {
        Usuario usuario = Usuario.builder().id(1L).role(Role.USUARIO).build();
        Usuario admin = Usuario.builder().id(10L).role(Role.ADMIN).build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .status(LivroStatus.EMPRESTADO)
                .admin(admin)
                .build();

        CriarEmprestimoRequest request = new CriarEmprestimoRequest(2L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(2L)).thenReturn(Optional.of(livro));

        RegraDeNegocioException exception = assertThrows(
                RegraDeNegocioException.class,
                () -> emprestimoService.criar(request, 1L)
        );

        assertEquals("O livro não está disponível para empréstimo.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoJaExistirEmprestimoAtivoParaLivro() {
        Usuario usuario = Usuario.builder().id(1L).role(Role.USUARIO).build();
        Usuario admin = Usuario.builder().id(10L).role(Role.ADMIN).build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        CriarEmprestimoRequest request = new CriarEmprestimoRequest(2L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(2L)).thenReturn(Optional.of(livro));
        when(emprestimoRepository.existsByLivroIdAndStatus(2L, EmprestimoStatus.ATIVO)).thenReturn(true);

        RegraDeNegocioException exception = assertThrows(
                RegraDeNegocioException.class,
                () -> emprestimoService.criar(request, 1L)
        );

        assertEquals("Já existe um empréstimo ativo para este livro.", exception.getMessage());
    }

    @Test
    void deveDevolverEmprestimoComSucessoSemInteressados() {
        Long adminId = 10L;

        Usuario admin = Usuario.builder().id(adminId).role(Role.ADMIN).build();
        Usuario usuario = Usuario.builder().id(1L).role(Role.USUARIO).build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .status(LivroStatus.EMPRESTADO)
                .admin(admin)
                .build();

        Emprestimo emprestimo = Emprestimo.builder()
                .id(100L)
                .usuario(usuario)
                .livro(livro)
                .dataEmprestimo(LocalDate.now().minusDays(3))
                .status(EmprestimoStatus.ATIVO)
                .build();

        when(emprestimoRepository.findById(100L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimo);
        when(interesseLivroRepository.findByLivroIdOrderByDataInteresseAsc(livro.getId()))
                .thenReturn(List.of());

        EmprestimoResponse response = emprestimoService.devolver(100L, adminId);

        assertEquals(EmprestimoStatus.FINALIZADO, response.status());
        assertEquals(LivroStatus.DISPONIVEL, livro.getStatus());
        assertNotNull(response.dataDevolucao());

        verify(livroRepository).save(livro);
        verify(interesseLivroRepository).findByLivroIdOrderByDataInteresseAsc(livro.getId());
        verify(notificacaoService, never()).criarNotificacao(any(), anyString(), anyString());
        verify(interesseLivroRepository, never()).deleteByLivroId(anyLong());
    }

    @Test
    void deveDevolverEmprestimoComSucessoENotificarInteressados() {
        Long adminId = 10L;

        Usuario admin = Usuario.builder().id(adminId).role(Role.ADMIN).build();
        Usuario usuarioDoEmprestimo = Usuario.builder().id(1L).role(Role.USUARIO).build();
        Usuario interessado1 = Usuario.builder().id(2L).role(Role.USUARIO).build();
        Usuario interessado2 = Usuario.builder().id(3L).role(Role.USUARIO).build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Clean Code")
                .status(LivroStatus.EMPRESTADO)
                .admin(admin)
                .build();

        Emprestimo emprestimo = Emprestimo.builder()
                .id(100L)
                .usuario(usuarioDoEmprestimo)
                .livro(livro)
                .dataEmprestimo(LocalDate.now().minusDays(3))
                .status(EmprestimoStatus.ATIVO)
                .build();

        InteresseLivro interesse1 = InteresseLivro.builder()
                .id(1L)
                .usuario(interessado1)
                .livro(livro)
                .dataInteresse(LocalDateTime.now().minusDays(2))
                .build();

        InteresseLivro interesse2 = InteresseLivro.builder()
                .id(2L)
                .usuario(interessado2)
                .livro(livro)
                .dataInteresse(LocalDateTime.now().minusDays(1))
                .build();

        when(emprestimoRepository.findById(100L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimo);
        when(interesseLivroRepository.findByLivroIdOrderByDataInteresseAsc(livro.getId()))
                .thenReturn(List.of(interesse1, interesse2));

        EmprestimoResponse response = emprestimoService.devolver(100L, adminId);

        assertEquals(EmprestimoStatus.FINALIZADO, response.status());
        assertEquals(LivroStatus.DISPONIVEL, livro.getStatus());
        assertNotNull(response.dataDevolucao());

        verify(livroRepository).save(livro);
        verify(interesseLivroRepository).findByLivroIdOrderByDataInteresseAsc(livro.getId());

        verify(notificacaoService).criarNotificacao(
                eq(interessado1),
                eq("Livro disponível novamente"),
                eq("O livro \"Clean Code\" está disponível para empréstimo novamente.")
        );
        verify(notificacaoService).criarNotificacao(
                eq(interessado2),
                eq("Livro disponível novamente"),
                eq("O livro \"Clean Code\" está disponível para empréstimo novamente.")
        );

        verify(interesseLivroRepository).deleteByLivroId(livro.getId());
    }

    @Test
    void deveLancarExcecaoAoDevolverEmprestimoDeOutroAdmin() {
        Long adminLogadoId = 10L;
        Long adminDonoLivroId = 20L;

        Usuario adminDono = Usuario.builder().id(adminDonoLivroId).role(Role.ADMIN).build();
        Usuario usuario = Usuario.builder().id(1L).role(Role.USUARIO).build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .status(LivroStatus.EMPRESTADO)
                .admin(adminDono)
                .build();

        Emprestimo emprestimo = Emprestimo.builder()
                .id(100L)
                .usuario(usuario)
                .livro(livro)
                .status(EmprestimoStatus.ATIVO)
                .build();

        when(emprestimoRepository.findById(100L)).thenReturn(Optional.of(emprestimo));

        PermissaoNegadaException exception = assertThrows(
                PermissaoNegadaException.class,
                () -> emprestimoService.devolver(100L, adminLogadoId)
        );

        assertEquals("Você não tem permissão para devolver este empréstimo.", exception.getMessage());
        verify(emprestimoRepository, never()).save(any());
        verify(livroRepository, never()).save(any());
        verify(interesseLivroRepository, never()).findByLivroIdOrderByDataInteresseAsc(anyLong());
        verify(notificacaoService, never()).criarNotificacao(any(), anyString(), anyString());
    }

    @Test
    void deveLancarExcecaoQuandoEmprestimoJaEstiverFinalizado() {
        Long adminId = 10L;

        Usuario admin = Usuario.builder().id(adminId).role(Role.ADMIN).build();
        Usuario usuario = Usuario.builder().id(1L).role(Role.USUARIO).build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        Emprestimo emprestimo = Emprestimo.builder()
                .id(100L)
                .usuario(usuario)
                .livro(livro)
                .status(EmprestimoStatus.FINALIZADO)
                .build();

        when(emprestimoRepository.findById(100L)).thenReturn(Optional.of(emprestimo));

        RegraDeNegocioException exception = assertThrows(
                RegraDeNegocioException.class,
                () -> emprestimoService.devolver(100L, adminId)
        );

        assertEquals("Este empréstimo já foi finalizado.", exception.getMessage());
        verify(interesseLivroRepository, never()).findByLivroIdOrderByDataInteresseAsc(anyLong());
        verify(notificacaoService, never()).criarNotificacao(any(), anyString(), anyString());
    }

    @Test
    void deveListarEmprestimosPorUsuario() {
        Usuario admin = Usuario.builder().id(10L).role(Role.ADMIN).build();
        Usuario usuario = Usuario.builder().id(1L).role(Role.USUARIO).build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .admin(admin)
                .build();

        Emprestimo emprestimo = Emprestimo.builder()
                .id(100L)
                .usuario(usuario)
                .livro(livro)
                .dataEmprestimo(LocalDate.now())
                .status(EmprestimoStatus.ATIVO)
                .build();

        when(emprestimoRepository.findByUsuarioId(1L)).thenReturn(List.of(emprestimo));

        List<EmprestimoResponse> response = emprestimoService.listarPorUsuario(1L);

        assertEquals(1, response.size());
        assertEquals(100L, response.get(0).id());
    }

    @Test
    void deveListarEmprestimosPorAdmin() {
        Usuario admin = Usuario.builder().id(10L).role(Role.ADMIN).build();
        Usuario usuario = Usuario.builder().id(1L).role(Role.USUARIO).build();

        Livro livro = Livro.builder()
                .id(2L)
                .titulo("Livro")
                .admin(admin)
                .build();

        Emprestimo emprestimo = Emprestimo.builder()
                .id(100L)
                .usuario(usuario)
                .livro(livro)
                .dataEmprestimo(LocalDate.now())
                .status(EmprestimoStatus.ATIVO)
                .build();

        when(emprestimoRepository.findByLivroAdminId(10L)).thenReturn(List.of(emprestimo));

        List<EmprestimoResponse> response = emprestimoService.listarPorAdmin(10L);

        assertEquals(1, response.size());
        assertEquals(100L, response.get(0).id());
    }
}