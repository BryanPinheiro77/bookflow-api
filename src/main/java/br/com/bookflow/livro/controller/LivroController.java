package br.com.bookflow.livro.controller;

import br.com.bookflow.livro.dto.AtualizarLivroRequest;
import br.com.bookflow.livro.dto.CadastrarLivroRequest;
import br.com.bookflow.livro.dto.LivroResponse;
import br.com.bookflow.livro.service.LivroService;
import br.com.bookflow.usuario.service.UsuarioAutenticadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/livros")
public class LivroController {

    private final LivroService livroService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public LivroController(LivroService livroService,
                           UsuarioAutenticadoService usuarioAutenticadoService) {
        this.livroService = livroService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public List<LivroResponse> listarTodos(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            Long adminId = usuarioAutenticadoService.buscarId(authentication);
            return livroService.listarPorAdmin(adminId);
        }

        return livroService.listarTodosParaUsuario();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public LivroResponse buscarPorId(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            Long adminId = usuarioAutenticadoService.buscarId(authentication);
            return livroService.buscarPorIdParaAdmin(id, adminId);
        }

        return livroService.buscarPorIdParaUsuario(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public LivroResponse cadastrar(@RequestBody @Valid CadastrarLivroRequest request,
                                   Authentication authentication) {
        Long adminId = usuarioAutenticadoService.buscarId(authentication);
        return livroService.cadastrar(request, adminId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public LivroResponse atualizar(@PathVariable Long id,
                                   @RequestBody @Valid AtualizarLivroRequest request,
                                   Authentication authentication) {
        Long adminId = usuarioAutenticadoService.buscarId(authentication);
        return livroService.atualizar(id, request, adminId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void excluir(@PathVariable Long id,
                        Authentication authentication) {
        Long adminId = usuarioAutenticadoService.buscarId(authentication);
        livroService.excluir(id, adminId);
    }

    @PostMapping("/{id}/capa")
    @PreAuthorize("hasRole('ADMIN')")
    public LivroResponse uploadCapa(@PathVariable Long id,
                                    @RequestParam("file") MultipartFile file,
                                    Authentication authentication) {
        Long adminId = usuarioAutenticadoService.buscarId(authentication);
        return livroService.uploadCapa(id, file, adminId);
    }

    @DeleteMapping("/{id}/capa")
    @PreAuthorize("hasRole('ADMIN')")
    public LivroResponse removerCapa(@PathVariable Long id,
                                     Authentication authentication) {
        Long adminId = usuarioAutenticadoService.buscarId(authentication);
        return livroService.removerCapa(id, adminId);
    }
}