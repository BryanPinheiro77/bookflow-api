package br.com.bookflow.interesse.controller;

import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.interesse.dto.InteresseLivroResponse;
import br.com.bookflow.interesse.service.InteresseLivroService;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interesses")
public class InteresseLivroController {

    private final InteresseLivroService interesseLivroService;
    private final UsuarioRepository usuarioRepository;

    public InteresseLivroController(InteresseLivroService interesseLivroService, UsuarioRepository usuarioRepository) {
        this.interesseLivroService = interesseLivroService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/livros/{livroId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USUARIO')")
    public InteresseLivroResponse registrarInteresse(@PathVariable Long livroId, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));

        return interesseLivroService.registrarInteresse(usuario.getId(), livroId);
    }

    @GetMapping("/meus")
    @PreAuthorize("hasRole('USUARIO')")
    public List<InteresseLivroResponse> listarMeusInteresses(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));

        return interesseLivroService.listarMeusInteresses(usuario.getId());
    }

    @DeleteMapping("/livros/{livroId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USUARIO')")
    public void removerInteresse(@PathVariable Long livroId, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));

        interesseLivroService.removerInteresse(usuario.getId(), livroId);
    }
}