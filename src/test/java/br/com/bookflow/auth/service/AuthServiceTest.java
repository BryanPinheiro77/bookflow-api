package br.com.bookflow.auth.service;

import br.com.bookflow.auth.dto.LoginRequest;
import br.com.bookflow.auth.dto.LoginResponse;
import br.com.bookflow.config.JwtService;
import br.com.bookflow.exception.CredenciaisInvalidasException;
import br.com.bookflow.usuario.entity.Role;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("bryan@email.com", "123456");

        usuario = Usuario.builder()
                .id(1L)
                .nome("Bryan")
                .email("bryan@email.com")
                .senha("senha-criptografada")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    void deveFazerLoginComSucesso() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenha())).thenReturn(true);
        when(jwtService.generateToken(usuario)).thenReturn("token-jwt-valido");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("token-jwt-valido", response.token());

        verify(usuarioRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder).matches(loginRequest.senha(), usuario.getSenha());
        verify(jwtService).generateToken(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoExistir() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Email ou senha inválidos", exception.getMessage());

        verify(usuarioRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void deveLancarExcecaoQuandoSenhaEstiverIncorreta() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenha())).thenReturn(false);

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Email ou senha inválidos", exception.getMessage());

        verify(usuarioRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder).matches(loginRequest.senha(), usuario.getSenha());
        verify(jwtService, never()).generateToken(any());
    }
}