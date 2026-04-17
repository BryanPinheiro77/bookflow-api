package br.com.bookflow.livro.service;

import br.com.bookflow.exception.PermissaoNegadaException;
import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.livro.dto.AtualizarLivroRequest;
import br.com.bookflow.livro.dto.CadastrarLivroRequest;
import br.com.bookflow.livro.dto.LivroResponse;
import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.livro.entity.LivroStatus;
import br.com.bookflow.livro.repository.LivroRepository;
import br.com.bookflow.usuario.entity.Role;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private LivroService livroService;

    @Test
    void deveCadastrarLivroComSucesso() {
        Long adminId = 1L;

        CadastrarLivroRequest request = new CadastrarLivroRequest(
                "Dom Casmurro",
                "Machado de Assis",
                "Romance",
                "https://exemplo.com/capa.jpg"
        );

        Usuario admin = Usuario.builder()
                .id(adminId)
                .nome("Admin")
                .email("admin@email.com")
                .senha("123")
                .role(Role.ADMIN)
                .build();

        Livro livroSalvo = Livro.builder()
                .id(10L)
                .titulo(request.titulo())
                .autor(request.autor())
                .categoria(request.categoria())
                .status(LivroStatus.DISPONIVEL)
                .capaUrl(request.capaUrl())
                .admin(admin)
                .build();

        when(usuarioRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(livroRepository.save(any(Livro.class))).thenReturn(livroSalvo);

        LivroResponse response = livroService.cadastrar(request, adminId);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Dom Casmurro", response.titulo());
        assertEquals(LivroStatus.DISPONIVEL, response.status());
        assertEquals(adminId, response.adminId());

        ArgumentCaptor<Livro> captor = ArgumentCaptor.forClass(Livro.class);
        verify(livroRepository).save(captor.capture());

        Livro livroCapturado = captor.getValue();
        assertEquals("Dom Casmurro", livroCapturado.getTitulo());
        assertEquals(LivroStatus.DISPONIVEL, livroCapturado.getStatus());
        assertEquals(admin, livroCapturado.getAdmin());
    }

    @Test
    void deveLancarExcecaoQuandoAdminNaoExistirAoCadastrar() {
        Long adminId = 1L;
        CadastrarLivroRequest request = new CadastrarLivroRequest(
                "Dom Casmurro",
                "Machado de Assis",
                "Romance",
                null
        );

        when(usuarioRepository.findById(adminId)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.cadastrar(request, adminId)
        );

        assertEquals("Administrador não encontrado.", exception.getMessage());
        verify(livroRepository, never()).save(any());
    }

    @Test
    void deveListarTodosOsLivros() {
        Usuario admin = Usuario.builder().id(1L).role(Role.ADMIN).build();

        Livro livro1 = Livro.builder()
                .id(1L)
                .titulo("Livro 1")
                .autor("Autor 1")
                .categoria("Categoria 1")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        Livro livro2 = Livro.builder()
                .id(2L)
                .titulo("Livro 2")
                .autor("Autor 2")
                .categoria("Categoria 2")
                .status(LivroStatus.EMPRESTADO)
                .admin(admin)
                .build();

        when(livroRepository.findAll()).thenReturn(List.of(livro1, livro2));

        List<LivroResponse> response = livroService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("Livro 1", response.get(0).titulo());
        assertEquals("Livro 2", response.get(1).titulo());
    }

    @Test
    void deveBuscarLivroPorIdComSucesso() {
        Usuario admin = Usuario.builder().id(1L).role(Role.ADMIN).build();

        Livro livro = Livro.builder()
                .id(1L)
                .titulo("Dom Casmurro")
                .autor("Machado")
                .categoria("Romance")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        LivroResponse response = livroService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Dom Casmurro", response.titulo());
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoExistirAoBuscarPorId() {
        when(livroRepository.findById(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.buscarPorId(1L)
        );

        assertEquals("Livro não encontrado.", exception.getMessage());
    }

    @Test
    void deveAtualizarLivroComSucesso() {
        Long adminId = 1L;

        AtualizarLivroRequest request = new AtualizarLivroRequest(
                "Livro Atualizado",
                "Autor Atualizado",
                "Categoria Atualizada",
                "https://exemplo.com/nova-capa.jpg"
        );

        Usuario admin = Usuario.builder().id(adminId).role(Role.ADMIN).build();

        Livro livro = Livro.builder()
                .id(1L)
                .titulo("Livro Antigo")
                .autor("Autor Antigo")
                .categoria("Categoria Antiga")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));
        when(livroRepository.save(any(Livro.class))).thenReturn(livro);

        LivroResponse response = livroService.atualizar(1L, request, adminId);

        assertEquals("Livro Atualizado", response.titulo());
        assertEquals("Autor Atualizado", response.autor());
        assertEquals("Categoria Atualizada", response.categoria());
        assertEquals("https://exemplo.com/nova-capa.jpg", response.capaUrl());
    }

    @Test
    void deveLancarExcecaoAoAtualizarLivroDeOutroAdmin() {
        Long adminLogadoId = 1L;
        Long adminDonoLivroId = 2L;

        AtualizarLivroRequest request = new AtualizarLivroRequest(
                "Livro Atualizado",
                "Autor Atualizado",
                "Categoria Atualizada",
                null
        );

        Usuario adminDono = Usuario.builder().id(adminDonoLivroId).role(Role.ADMIN).build();

        Livro livro = Livro.builder()
                .id(1L)
                .titulo("Livro")
                .autor("Autor")
                .categoria("Categoria")
                .status(LivroStatus.DISPONIVEL)
                .admin(adminDono)
                .build();

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        PermissaoNegadaException exception = assertThrows(
                PermissaoNegadaException.class,
                () -> livroService.atualizar(1L, request, adminLogadoId)
        );

        assertEquals("Você não tem permissão para alterar este livro.", exception.getMessage());
        verify(livroRepository, never()).save(any());
    }

    @Test
    void deveExcluirLivroComSucesso() {
        Long adminId = 1L;

        Usuario admin = Usuario.builder().id(adminId).role(Role.ADMIN).build();

        Livro livro = Livro.builder()
                .id(1L)
                .titulo("Livro")
                .autor("Autor")
                .categoria("Categoria")
                .status(LivroStatus.DISPONIVEL)
                .admin(admin)
                .build();

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        livroService.excluir(1L, adminId);

        verify(livroRepository).delete(livro);
    }

    @Test
    void deveLancarExcecaoAoExcluirLivroDeOutroAdmin() {
        Long adminLogadoId = 1L;
        Long adminDonoLivroId = 2L;

        Usuario adminDono = Usuario.builder().id(adminDonoLivroId).role(Role.ADMIN).build();

        Livro livro = Livro.builder()
                .id(1L)
                .titulo("Livro")
                .autor("Autor")
                .categoria("Categoria")
                .status(LivroStatus.DISPONIVEL)
                .admin(adminDono)
                .build();

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        PermissaoNegadaException exception = assertThrows(
                PermissaoNegadaException.class,
                () -> livroService.excluir(1L, adminLogadoId)
        );

        assertEquals("Você não tem permissão para alterar este livro.", exception.getMessage());
        verify(livroRepository, never()).delete(any());
    }
}