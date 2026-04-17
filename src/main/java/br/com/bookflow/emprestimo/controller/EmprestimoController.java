package br.com.bookflow.emprestimo.controller;

import br.com.bookflow.emprestimo.dto.CriarEmprestimoRequest;
import br.com.bookflow.emprestimo.dto.EmprestimoResponse;
import br.com.bookflow.emprestimo.service.EmprestimoService;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;
    private final UsuarioRepository usuarioRepository;

    public EmprestimoController(EmprestimoService emprestimoService,
                                UsuarioRepository usuarioRepository) {
        this.emprestimoService = emprestimoService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USUARIO')")
    public EmprestimoResponse criar(@RequestBody @Valid CriarEmprestimoRequest request,
                                    Authentication authentication) {
        Long usuarioId = getUsuarioId(authentication);
        return emprestimoService.criar(request, usuarioId);
    }

    @PutMapping("/{id}/devolver")
    @PreAuthorize("hasRole('ADMIN')")
    public EmprestimoResponse devolver(@PathVariable Long id) {
        return emprestimoService.devolver(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<EmprestimoResponse> listarTodos() {
        return emprestimoService.listarTodos();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USUARIO')")
    public List<EmprestimoResponse> listarMeusEmprestimos(Authentication authentication) {
        Long usuarioId = getUsuarioId(authentication);
        return emprestimoService.listarPorUsuario(usuarioId);
    }

    private Long getUsuarioId(Authentication authentication) {
        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));

        return usuario.getId();
    }
}