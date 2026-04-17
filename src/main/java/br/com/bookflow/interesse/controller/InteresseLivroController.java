package br.com.bookflow.interesse.controller;

import br.com.bookflow.interesse.dto.InteresseLivroResponse;
import br.com.bookflow.interesse.service.InteresseLivroService;
import br.com.bookflow.usuario.service.UsuarioAutenticadoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interesses")
public class InteresseLivroController {

    private final InteresseLivroService interesseLivroService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public InteresseLivroController(
            InteresseLivroService interesseLivroService,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.interesseLivroService = interesseLivroService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @PostMapping("/livros/{livroId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USUARIO')")
    public InteresseLivroResponse registrarInteresse(@PathVariable Long livroId, Authentication authentication) {
        Long usuarioId = usuarioAutenticadoService.buscarId(authentication);
        return interesseLivroService.registrarInteresse(usuarioId, livroId);
    }

    @GetMapping("/meus")
    @PreAuthorize("hasRole('USUARIO')")
    public List<InteresseLivroResponse> listarMeusInteresses(Authentication authentication) {
        Long usuarioId = usuarioAutenticadoService.buscarId(authentication);
        return interesseLivroService.listarMeusInteresses(usuarioId);
    }

    @DeleteMapping("/livros/{livroId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USUARIO')")
    public void removerInteresse(@PathVariable Long livroId, Authentication authentication) {
        Long usuarioId = usuarioAutenticadoService.buscarId(authentication);
        interesseLivroService.removerInteresse(usuarioId, livroId);
    }
}