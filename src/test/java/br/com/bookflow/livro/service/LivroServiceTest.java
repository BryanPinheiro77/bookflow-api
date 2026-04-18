package br.com.bookflow.livro.service;

import br.com.bookflow.exception.PermissaoNegadaException;
import br.com.bookflow.exception.RecursoNaoEncontradoException;
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
                null
        );

        Livro livroSalvo = Livro.builder()
                .id(10L)
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .categoria("Programação")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .capaUrl(null)
                .build();

        when(usuarioRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(livroRepository.save(any(Livro.class))).thenReturn(livroSalvo);

        LivroResponse response = livroService.cadastrar(request, adminId);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Clean Code", response.titulo());
        assertEquals("Robert C. Martin", response.autor());
        assertEquals("Programação", response.categoria());
        assertEquals(LivroStatus.DISPONIVEL, response.status());
    }

    @Test
    void deveLancarExcecaoQuandoAdminNaoForEncontradoAoCadastrar() {
        CadastrarLivroRequest request = new CadastrarLivroRequest(
                "Clean Code",
                "Robert C. Martin",
                "Programação",
                null
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.cadastrar(request, 1L)
        );

        assertEquals("Administrador não encontrado.", exception.getMessage());
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
                .status(LivroStatus.DISPONIVEL)
                .admin(admin1)
                .build();

        Livro livro2 = Livro.builder()
                .id(11L)
                .titulo("DDD")
                .autor("Eric Evans")
                .categoria("Arquitetura")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin2)
                .build();

        when(livroRepository.findAll()).thenReturn(List.of(livro1, livro2));

        List<LivroResponse> response = livroService.listarTodosParaUsuario();

        assertEquals(2, response.size());
        assertEquals("Clean Code", response.get(0).titulo());
        assertEquals("DDD", response.get(1).titulo());
    }

    @Test
    void deveListarLivrosDoAdmin() {
        Long adminId = 1L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .role(Role.ADMIN)
                .build();

        Livro livro1 = Livro.builder()
                .id(10L)
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .categoria("Programação")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        Livro livro2 = Livro.builder()
                .id(11L)
                .titulo("DDD")
                .autor("Eric Evans")
                .categoria("Arquitetura")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        when(livroRepository.findByAdminId(adminId)).thenReturn(List.of(livro1, livro2));

        List<LivroResponse> response = livroService.listarPorAdmin(adminId);

        assertEquals(2, response.size());
        assertEquals("Clean Code", response.get(0).titulo());
        assertEquals("DDD", response.get(1).titulo());
    }

    @Test
    void deveBuscarLivroPorIdParaUsuarioComSucesso() {
        Usuario admin = Usuario.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(10L)
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .categoria("Programação")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .capaUrl("/uploads/capas/capa.jpg")
                .build();

        when(livroRepository.findById(10L)).thenReturn(Optional.of(livro));

        LivroResponse response = livroService.buscarPorIdParaUsuario(10L);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Clean Code", response.titulo());
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoForEncontradoAoBuscarPorIdParaUsuario() {
        when(livroRepository.findById(10L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.buscarPorIdParaUsuario(10L)
        );

        assertEquals("Livro não encontrado.", exception.getMessage());
    }

    @Test
    void deveBuscarLivroPorIdParaAdminComSucesso() {
        Long adminId = 1L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(10L)
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .categoria("Programação")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .capaUrl("/uploads/capas/capa.jpg")
                .build();

        when(livroRepository.findByIdAndAdminId(10L, adminId)).thenReturn(Optional.of(livro));

        LivroResponse response = livroService.buscarPorIdParaAdmin(10L, adminId);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Clean Code", response.titulo());
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoPertencerAoAdminAoBuscarPorIdParaAdmin() {
        when(livroRepository.findByIdAndAdminId(10L, 1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.buscarPorIdParaAdmin(10L, 1L)
        );

        assertEquals("Livro não encontrado.", exception.getMessage());
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
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        AtualizarLivroRequest request = new AtualizarLivroRequest(
                "Novo Título",
                "Novo Autor",
                "Nova Categoria",
                "/uploads/capas/nova.jpg"
        );

        when(livroRepository.findById(10L)).thenReturn(Optional.of(livro));
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LivroResponse response = livroService.atualizar(10L, request, adminId);

        assertEquals("Novo Título", response.titulo());
        assertEquals("Novo Autor", response.autor());
        assertEquals("Nova Categoria", response.categoria());
        assertEquals("/uploads/capas/nova.jpg", response.capaUrl());
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
                .status(LivroStatus.DISPONIVEL)
                .admin(adminDono)
                .build();

        AtualizarLivroRequest request = new AtualizarLivroRequest(
                "Novo Título",
                "Novo Autor",
                "Nova Categoria",
                null
        );

        when(livroRepository.findById(10L)).thenReturn(Optional.of(livro));

        PermissaoNegadaException exception = assertThrows(
                PermissaoNegadaException.class,
                () -> livroService.atualizar(10L, request, 2L)
        );

        assertEquals("Você não tem permissão para alterar este livro.", exception.getMessage());
    }

    @Test
    void deveExcluirLivroComSucesso() {
        Long adminId = 1L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(10L)
                .titulo("Livro")
                .autor("Autor")
                .categoria("Categoria")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        when(livroRepository.findById(10L)).thenReturn(Optional.of(livro));

        livroService.excluir(10L, adminId);

        verify(livroRepository).delete(livro);
    }

    @Test
    void deveLancarExcecaoQuandoTentarExcluirLivroDeOutroAdmin() {
        Usuario adminDono = Usuario.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(10L)
                .titulo("Livro")
                .autor("Autor")
                .categoria("Categoria")
                .status(LivroStatus.DISPONIVEL)
                .admin(adminDono)
                .build();

        when(livroRepository.findById(10L)).thenReturn(Optional.of(livro));

        PermissaoNegadaException exception = assertThrows(
                PermissaoNegadaException.class,
                () -> livroService.excluir(10L, 2L)
        );

        assertEquals("Você não tem permissão para alterar este livro.", exception.getMessage());
    }

    @Test
    void deveFazerUploadDeCapaComSucesso() {
        Long adminId = 1L;
        Long livroId = 10L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .nome("Admin")
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(livroId)
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .categoria("Programação")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .capaUrl(null)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "capa.jpg",
                "image/jpeg",
                "conteudo fake".getBytes()
        );

        UploadResponse uploadResponse = new UploadResponse(
                "uuid-capa.jpg",
                "/uploads/capas/uuid-capa.jpg"
        );

        when(livroRepository.findById(livroId)).thenReturn(Optional.of(livro));
        when(uploadService.uploadCapaLivro(file)).thenReturn(uploadResponse);
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LivroResponse response = livroService.uploadCapa(livroId, file, adminId);

        assertNotNull(response);
        assertEquals("/uploads/capas/uuid-capa.jpg", response.capaUrl());
        assertEquals("/uploads/capas/uuid-capa.jpg", livro.getCapaUrl());

        verify(uploadService).uploadCapaLivro(file);
        verify(livroRepository).save(livro);
    }

    @Test
    void deveRemoverCapaAnteriorAoFazerNovoUpload() {
        Long adminId = 1L;
        Long livroId = 10L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(livroId)
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .categoria("Programação")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .capaUrl("/uploads/capas/capa-antiga.jpg")
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "capa-nova.jpg",
                "image/jpeg",
                "conteudo fake".getBytes()
        );

        UploadResponse uploadResponse = new UploadResponse(
                "uuid-capa-nova.jpg",
                "/uploads/capas/uuid-capa-nova.jpg"
        );

        when(livroRepository.findById(livroId)).thenReturn(Optional.of(livro));
        when(uploadService.uploadCapaLivro(file)).thenReturn(uploadResponse);
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LivroResponse response = livroService.uploadCapa(livroId, file, adminId);

        assertEquals("/uploads/capas/uuid-capa-nova.jpg", response.capaUrl());
        verify(uploadService).removerArquivo("/uploads/capas/capa-antiga.jpg");
        verify(livroRepository).save(livro);
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoExistirAoFazerUploadDeCapa() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "capa.jpg",
                "image/jpeg",
                "conteudo fake".getBytes()
        );

        when(livroRepository.findById(10L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.uploadCapa(10L, file, 1L)
        );

        assertEquals("Livro não encontrado.", exception.getMessage());
        verify(uploadService, never()).uploadCapaLivro(any());
        verify(livroRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoAdminNaoForDonoDoLivroAoFazerUploadDeCapa() {
        Usuario adminDono = Usuario.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(10L)
                .titulo("Livro")
                .autor("Autor")
                .categoria("Categoria")
                .status(LivroStatus.DISPONIVEL)
                .admin(adminDono)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "capa.jpg",
                "image/jpeg",
                "conteudo fake".getBytes()
        );

        when(livroRepository.findById(10L)).thenReturn(Optional.of(livro));

        PermissaoNegadaException exception = assertThrows(
                PermissaoNegadaException.class,
                () -> livroService.uploadCapa(10L, file, 2L)
        );

        assertEquals("Você não tem permissão para alterar a capa deste livro.", exception.getMessage());
        verify(uploadService, never()).uploadCapaLivro(any());
        verify(livroRepository, never()).save(any());
    }

    @Test
    void deveRemoverCapaComSucesso() {
        Long adminId = 1L;
        Long livroId = 10L;

        Usuario admin = Usuario.builder()
                .id(adminId)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(livroId)
                .titulo("Livro")
                .autor("Autor")
                .categoria("Categoria")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .capaUrl("/uploads/capas/capa.jpg")
                .build();

        when(livroRepository.findById(livroId)).thenReturn(Optional.of(livro));
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LivroResponse response = livroService.removerCapa(livroId, adminId);

        assertNotNull(response);
        assertNull(response.capaUrl());
        assertNull(livro.getCapaUrl());

        verify(uploadService).removerArquivo("/uploads/capas/capa.jpg");
        verify(livroRepository).save(livro);
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoExistirAoRemoverCapa() {
        when(livroRepository.findById(10L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.removerCapa(10L, 1L)
        );

        assertEquals("Livro não encontrado.", exception.getMessage());
        verify(uploadService, never()).removerArquivo(any());
        verify(livroRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoAdminNaoForDonoDoLivroAoRemoverCapa() {
        Usuario adminDono = Usuario.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();

        Livro livro = Livro.builder()
                .id(10L)
                .titulo("Livro")
                .autor("Autor")
                .categoria("Categoria")
                .status(LivroStatus.DISPONIVEL)
                .admin(adminDono)
                .capaUrl("/uploads/capas/capa.jpg")
                .build();

        when(livroRepository.findById(10L)).thenReturn(Optional.of(livro));

        PermissaoNegadaException exception = assertThrows(
                PermissaoNegadaException.class,
                () -> livroService.removerCapa(10L, 2L)
        );

        assertEquals("Você não tem permissão para remover a capa deste livro.", exception.getMessage());
        verify(uploadService, never()).removerArquivo(any());
        verify(livroRepository, never()).save(any());
    }
}