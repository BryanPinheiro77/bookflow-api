package br.com.bookflow.usuario.service;

import br.com.bookflow.usuario.dto.CadastrarUsuarioRequest;
import br.com.bookflow.usuario.dto.UsuarioResponse;
import br.com.bookflow.usuario.entity.Role;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private CadastrarUsuarioRequest cadastrarUsuarioRequest;

    @BeforeEach
    void setUp() {
        cadastrarUsuarioRequest = new CadastrarUsuarioRequest(
                "Bryan",
                "bryan@email.com",
                "123456",
                Role.ADMIN
        );
    }

    @Test
    void deveCadastrarUsuarioComSucesso() {
        when(usuarioRepository.existsByEmail(cadastrarUsuarioRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(cadastrarUsuarioRequest.senha())).thenReturn("senha-criptografada");

        Usuario usuarioSalvo = Usuario.builder()
                .id(1L)
                .nome(cadastrarUsuarioRequest.nome())
                .email(cadastrarUsuarioRequest.email())
                .senha("senha-criptografada")
                .role(cadastrarUsuarioRequest.role())
                .build();

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        UsuarioResponse response = usuarioService.cadastrar(cadastrarUsuarioRequest);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Bryan", response.nome());
        assertEquals("bryan@email.com", response.email());
        assertEquals(Role.ADMIN, response.role());

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioCapturado = usuarioCaptor.getValue();
        assertEquals("Bryan", usuarioCapturado.getNome());
        assertEquals("bryan@email.com", usuarioCapturado.getEmail());
        assertEquals("senha-criptografada", usuarioCapturado.getSenha());
        assertEquals(Role.ADMIN, usuarioCapturado.getRole());

        verify(usuarioRepository).existsByEmail(cadastrarUsuarioRequest.email());
        verify(passwordEncoder).encode(cadastrarUsuarioRequest.senha());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaEstiverCadastrado() {
        when(usuarioRepository.existsByEmail(cadastrarUsuarioRequest.email())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.cadastrar(cadastrarUsuarioRequest));

        assertEquals("Já existe um usuário cadastrado com este email.", exception.getMessage());

        verify(usuarioRepository).existsByEmail(cadastrarUsuarioRequest.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}