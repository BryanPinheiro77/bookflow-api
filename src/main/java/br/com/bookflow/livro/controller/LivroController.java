package br.com.bookflow.livro.controller;

import br.com.bookflow.livro.dto.AtualizarLivroRequest;
import br.com.bookflow.livro.dto.CadastrarLivroRequest;
import br.com.bookflow.livro.dto.LivroResponse;
import br.com.bookflow.livro.service.LivroService;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livros")
public class LivroController {

    private final LivroService livroService;
    private final UsuarioRepository usuarioRepository;

    public LivroController(LivroService livroService, UsuarioRepository usuarioRepository) {
        this.livroService = livroService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public List<LivroResponse> listarTodos() {
        return livroService.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public LivroResponse buscarPorId(@PathVariable Long id) {
        return livroService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public LivroResponse cadastrar(@RequestBody @Valid CadastrarLivroRequest request,
                                   Authentication authentication) {
        Long adminId = getAdminId(authentication);
        return livroService.cadastrar(request, adminId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public LivroResponse atualizar(@PathVariable Long id,
                                   @RequestBody @Valid AtualizarLivroRequest request,
                                   Authentication authentication) {
        Long adminId = getAdminId(authentication);
        return livroService.atualizar(id, request, adminId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void excluir(@PathVariable Long id,
                        Authentication authentication) {
        Long adminId = getAdminId(authentication);
        livroService.excluir(id, adminId);
    }

    private Long getAdminId(Authentication authentication) {
        String email = authentication.getName();

        Usuario admin = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador autenticado não encontrado."));

        return admin.getId();
    }
}