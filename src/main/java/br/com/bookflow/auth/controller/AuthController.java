package br.com.bookflow.auth.controller;

import br.com.bookflow.auth.dto.LoginRequest;
import br.com.bookflow.auth.dto.LoginResponse;
import br.com.bookflow.auth.service.AuthService;
import br.com.bookflow.usuario.dto.CadastrarUsuarioRequest;
import br.com.bookflow.usuario.dto.UsuarioResponse;
import br.com.bookflow.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/cadastrar")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody @Valid CadastrarUsuarioRequest request) {
        return usuarioService.cadastrar(request);
    }
}