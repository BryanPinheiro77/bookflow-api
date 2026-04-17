package br.com.bookflow.auth.service;

import br.com.bookflow.auth.dto.LoginRequest;
import br.com.bookflow.auth.dto.LoginResponse;
import br.com.bookflow.config.JwtService;
import br.com.bookflow.exception.CredenciaisInvalidasException;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new CredenciaisInvalidasException("Email ou senha inválidos"));

        boolean senhaCorreta = passwordEncoder.matches(request.senha(), usuario.getSenha());

        if (!senhaCorreta) {
            throw new CredenciaisInvalidasException("Email ou senha inválidos");
        }

        String token = jwtService.generateToken(usuario);

        return new LoginResponse(token);
    }
}