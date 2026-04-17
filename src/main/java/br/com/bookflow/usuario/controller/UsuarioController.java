package br.com.bookflow.usuario.controller;

import br.com.bookflow.usuario.dto.CadastrarUsuarioRequest;
import br.com.bookflow.usuario.dto.UsuarioResponse;
import br.com.bookflow.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody @Valid CadastrarUsuarioRequest request) {
        return usuarioService.cadastrar(request);
    }
}