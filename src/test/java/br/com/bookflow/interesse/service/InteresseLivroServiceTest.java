package br.com.bookflow.interesse.service;

import br.com.bookflow.exception.RegraDeNegocioException;
import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.interesse.entity.InteresseLivro;
import br.com.bookflow.interesse.repository.InteresseLivroRepository;
import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.livro.entity.LivroStatus;
import br.com.bookflow.livro.repository.LivroRepository;
import br.com.bookflow.usuario.entity.Role;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InteresseLivroServiceTest {

    private InteresseLivroRepository interesseLivroRepository;
    private LivroRepository livroRepository;
    private UsuarioRepository usuarioRepository;
    private InteresseLivroService interesseLivroService;

    @BeforeEach
    void setUp() {
        interesseLivroRepository = mock(InteresseLivroRepository.class);
        livroRepository = mock(LivroRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);

        interesseLivroService = new InteresseLivroService(
                interesseLivroRepository,
                livroRepository,
                usuarioRepository
        );
    }

    @Test
    void deveRegistrarInteresseComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Bryan");
        usuario.setEmail("bryan@email.com");
        usuario.setSenha("123");
        usuario.setRole(Role.USUARIO);

        Livro livro = new Livro();
        livro.setId(10L);
        livro.setTitulo("Clean Code");
        livro.setAutor("Robert Martin");
        livro.setStatus(LivroStatus.EMPRESTADO);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(10L)).thenReturn(Optional.of(livro));
        when(interesseLivroRepository.existsByUsuarioIdAndLivroId(1L, 10L)).thenReturn(false);

        when(interesseLivroRepository.save(any(InteresseLivro.class)))
                .thenAnswer(invocation -> {
                    InteresseLivro interesse = invocation.getArgument(0);
                    return InteresseLivro.builder()
                            .id(1L)
                            .usuario(usuario)
                            .livro(livro)
                            .dataInteresse(interesse.getDataInteresse())
                            .build();
                });

        var response = interesseLivroService.registrarInteresse(1L, 10L);

        assertEquals(10L, response.livroId());
        assertEquals("Clean Code", response.tituloLivro());
        assertEquals("Robert Martin", response.autorLivro());

        ArgumentCaptor<InteresseLivro> captor = ArgumentCaptor.forClass(InteresseLivro.class);
        verify(interesseLivroRepository).save(captor.capture());

        InteresseLivro salvo = captor.getValue();
        assertEquals(usuario, salvo.getUsuario());
        assertEquals(livro, salvo.getLivro());
        assertNotNull(salvo.getDataInteresse());
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoEstiverEmprestado() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Livro livro = new Livro();
        livro.setId(10L);
        livro.setStatus(LivroStatus.DISPONIVEL);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(10L)).thenReturn(Optional.of(livro));

        assertThrows(RegraDeNegocioException.class,
                () -> interesseLivroService.registrarInteresse(1L, 10L));
    }

    @Test
    void deveLancarExcecaoQuandoInteresseJaExistir() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Livro livro = new Livro();
        livro.setId(10L);
        livro.setStatus(LivroStatus.EMPRESTADO);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(10L)).thenReturn(Optional.of(livro));
        when(interesseLivroRepository.existsByUsuarioIdAndLivroId(1L, 10L)).thenReturn(true);

        assertThrows(RegraDeNegocioException.class,
                () -> interesseLivroService.registrarInteresse(1L, 10L));
    }

    @Test
    void deveListarMeusInteresses() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Livro livro = new Livro();
        livro.setId(10L);
        livro.setTitulo("DDD");
        livro.setAutor("Eric Evans");

        InteresseLivro interesse = InteresseLivro.builder()
                .id(1L)
                .usuario(usuario)
                .livro(livro)
                .dataInteresse(LocalDateTime.now())
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(interesseLivroRepository.findByUsuarioIdOrderByDataInteresseDesc(1L))
                .thenReturn(List.of(interesse));

        var lista = interesseLivroService.listarMeusInteresses(1L);

        assertEquals(1, lista.size());
        assertEquals("DDD", lista.get(0).tituloLivro());
    }

    @Test
    void deveRemoverInteresseComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Livro livro = new Livro();
        livro.setId(10L);

        InteresseLivro interesse = InteresseLivro.builder()
                .id(1L)
                .usuario(usuario)
                .livro(livro)
                .dataInteresse(LocalDateTime.now())
                .build();

        when(interesseLivroRepository.findByUsuarioIdAndLivroId(1L, 10L))
                .thenReturn(Optional.of(interesse));

        interesseLivroService.removerInteresse(1L, 10L);

        verify(interesseLivroRepository).delete(interesse);
    }

    @Test
    void deveLancarExcecaoQuandoInteresseNaoExistirAoRemover() {
        when(interesseLivroRepository.findByUsuarioIdAndLivroId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> interesseLivroService.removerInteresse(1L, 10L));
    }
}