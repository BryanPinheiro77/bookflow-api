package br.com.bookflow.livro.service;

import br.com.bookflow.exception.PermissaoNegadaException;
import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.exception.RegraDeNegocioException;
import br.com.bookflow.livro.dto.AtualizarLivroRequest;
import br.com.bookflow.livro.dto.CadastrarLivroRequest;
import br.com.bookflow.livro.dto.LivroResponse;
import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.livro.entity.LivroStatus;
import br.com.bookflow.livro.repository.LivroRepository;
import br.com.bookflow.upload.dto.UploadResponse;
import br.com.bookflow.upload.service.UploadService;
import br.com.bookflow.usuario.entity.Role;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LivroServiceTest {

    private LivroRepository livroRepository;
    private UsuarioRepository usuarioRepository;
    private UploadService uploadService;
    private LivroService livroService;

    @BeforeEach
    void setUp() {

        livroRepository = mock(LivroRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        uploadService = mock(UploadService.class);

        livroService = new LivroService(
                livroRepository,
                usuarioRepository,
                uploadService
        );
    }

    @Test
    void deveCadastrarLivroComSucesso() {

        Long adminId = 1L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .nome("Admin")
                .email("admin@email.com")
                .senha("123456")
                .role(Role.ADMIN)
                .build();

        CadastrarLivroRequest request = new CadastrarLivroRequest(
                "Clean Code",
                "Robert C. Martin",
                "Programação",
                10,
                8,
                new BigDecimal("12.90"),
                new BigDecimal("1.50"),
                null
        );

        Livro livroSalvo = Livro.builder()
                .id(10L)
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .categoria("Programação")
                .quantidadeTotal(10)
                .quantidadeDisponivel(8)
                .valorEmprestimo(new BigDecimal("12.90"))
                .valorMultaDiaria(new BigDecimal("1.50"))
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .capaUrl(null)
                .build();

        when(usuarioRepository.findById(adminId))
                .thenReturn(Optional.of(admin));

        when(livroRepository.save(any(Livro.class)))
                .thenReturn(livroSalvo);

        LivroResponse response =
                livroService.cadastrar(request, adminId);

        assertNotNull(response);

        assertEquals(10L, response.id());
        assertEquals("Clean Code", response.titulo());
        assertEquals("Robert C. Martin", response.autor());

        assertEquals(10, response.quantidadeTotal());
        assertEquals(8, response.quantidadeDisponivel());

        assertEquals(
                new BigDecimal("12.90"),
                response.valorEmprestimo()
        );

        assertEquals(
                new BigDecimal("1.50"),
                response.valorMultaDiaria()
        );

        assertEquals(
                LivroStatus.DISPONIVEL,
                response.status()
        );
    }

    @Test
    void deveLancarExcecaoQuandoQuantidadeDisponivelForMaiorQueTotal() {

        Long adminId = 1L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .role(Role.ADMIN)
                .build();

        CadastrarLivroRequest request = new CadastrarLivroRequest(
                "Livro",
                "Autor",
                "Categoria",
                5,
                10,
                new BigDecimal("10.00"),
                new BigDecimal("1.00"),
                null
        );

        when(usuarioRepository.findById(adminId))
                .thenReturn(Optional.of(admin));

        RegraDeNegocioException exception = assertThrows(
                RegraDeNegocioException.class,
                () -> livroService.cadastrar(request, adminId)
        );

        assertEquals(
                "Quantidade disponível não pode ser maior que a total.",
                exception.getMessage()
        );
    }

    @Test
    void deveDefinirLivroComoIndisponivelQuandoQuantidadeForZero() {

        Long adminId = 1L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .role(Role.ADMIN)
                .build();

        CadastrarLivroRequest request = new CadastrarLivroRequest(
                "Livro",
                "Autor",
                "Categoria",
                5,
                0,
                new BigDecimal("10.00"),
                new BigDecimal("1.00"),
                null
        );

        Livro livroSalvo = Livro.builder()
                .id(1L)
                .titulo("Livro")
                .autor("Autor")
                .categoria("Categoria")
                .quantidadeTotal(5)
                .quantidadeDisponivel(0)
                .valorEmprestimo(new BigDecimal("10.00"))
                .valorMultaDiaria(new BigDecimal("1.00"))
                .status(LivroStatus.INDISPONIVEL)
                .admin(admin)
                .build();

        when(usuarioRepository.findById(adminId))
                .thenReturn(Optional.of(admin));

        when(livroRepository.save(any(Livro.class)))
                .thenReturn(livroSalvo);

        LivroResponse response =
                livroService.cadastrar(request, adminId);

        assertEquals(
                LivroStatus.INDISPONIVEL,
                response.status()
        );
    }

    @Test
    void deveLancarExcecaoQuandoAdminNaoForEncontradoAoCadastrar() {

        CadastrarLivroRequest request = new CadastrarLivroRequest(
                "Clean Code",
                "Robert C. Martin",
                "Programação",
                10,
                8,
                new BigDecimal("12.90"),
                new BigDecimal("1.50"),
                null
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.cadastrar(request, 1L)
        );

        assertEquals(
                "Administrador não encontrado.",
                exception.getMessage()
        );
    }

    @Test
    void deveListarTodosOsLivrosParaUsuario() {

        Usuario admin1 = Usuario.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();

        Usuario admin2 = Usuario.builder()
                .id(2L)
                .role(Role.ADMIN)
                .build();

        Livro livro1 = Livro.builder()
                .id(10L)
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .categoria("Programação")
                .quantidadeTotal(10)
                .quantidadeDisponivel(8)
                .valorEmprestimo(new BigDecimal("12.90"))
                .valorMultaDiaria(new BigDecimal("1.50"))
                .status(LivroStatus.DISPONIVEL)
                .admin(admin1)
                .build();

        Livro livro2 = Livro.builder()
                .id(11L)
                .titulo("DDD")
                .autor("Eric Evans")
                .categoria("Arquitetura")
                .quantidadeTotal(5)
                .quantidadeDisponivel(5)
                .valorEmprestimo(new BigDecimal("15.90"))
                .valorMultaDiaria(new BigDecimal("2.00"))
                .status(LivroStatus.DISPONIVEL)
                .admin(admin2)
                .build();

        when(livroRepository.findAll())
                .thenReturn(List.of(livro1, livro2));

        List<LivroResponse> response =
                livroService.listarTodosParaUsuario();

        assertEquals(2, response.size());
        assertEquals("Clean Code", response.get(0).titulo());
        assertEquals("DDD", response.get(1).titulo());
    }

    @Test
    void deveAtualizarLivroComSucesso() {

        Long adminId = 1L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(10L)
                .titulo("Antigo")
                .autor("Autor Antigo")
                .categoria("Categoria Antiga")
                .quantidadeTotal(5)
                .quantidadeDisponivel(5)
                .valorEmprestimo(new BigDecimal("10.00"))
                .valorMultaDiaria(new BigDecimal("1.00"))
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        AtualizarLivroRequest request = new AtualizarLivroRequest(
                "Novo Título",
                "Novo Autor",
                "Nova Categoria",
                10,
                8,
                new BigDecimal("15.90"),
                new BigDecimal("2.00"),
                null
        );

        when(livroRepository.findById(10L))
                .thenReturn(Optional.of(livro));

        when(livroRepository.save(any(Livro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LivroResponse response =
                livroService.atualizar(10L, request, adminId);

        assertEquals("Novo Título", response.titulo());
        assertEquals("Novo Autor", response.autor());
        assertEquals("Nova Categoria", response.categoria());

        assertEquals(10, response.quantidadeTotal());
        assertEquals(8, response.quantidadeDisponivel());

        assertEquals(
                new BigDecimal("15.90"),
                response.valorEmprestimo()
        );

        assertEquals(
                new BigDecimal("2.00"),
                response.valorMultaDiaria()
        );
    }

    @Test
    void deveLancarExcecaoQuandoTentarAtualizarLivroDeOutroAdmin() {

        Usuario adminDono = Usuario.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(10L)
                .titulo("Livro")
                .autor("Autor")
                .categoria("Categoria")
                .quantidadeTotal(5)
                .quantidadeDisponivel(5)
                .valorEmprestimo(new BigDecimal("10.00"))
                .valorMultaDiaria(new BigDecimal("1.00"))
                .status(LivroStatus.DISPONIVEL)
                .admin(adminDono)
                .build();

        AtualizarLivroRequest request = new AtualizarLivroRequest(
                "Novo Título",
                "Novo Autor",
                "Nova Categoria",
                10,
                8,
                new BigDecimal("15.90"),
                new BigDecimal("2.00"),
                null
        );

        when(livroRepository.findById(10L))
                .thenReturn(Optional.of(livro));

        PermissaoNegadaException exception = assertThrows(
                PermissaoNegadaException.class,
                () -> livroService.atualizar(10L, request, 2L)
        );

        assertEquals(
                "Você não tem permissão para alterar este livro.",
                exception.getMessage()
        );
    }
}