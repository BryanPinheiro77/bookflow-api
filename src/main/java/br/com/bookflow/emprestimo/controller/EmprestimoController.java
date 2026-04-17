package br.com.bookflow.emprestimo.controller;

import br.com.bookflow.emprestimo.dto.CriarEmprestimoRequest;
import br.com.bookflow.emprestimo.dto.EmprestimoResponse;
import br.com.bookflow.emprestimo.service.EmprestimoService;
import br.com.bookflow.usuario.service.UsuarioAutenticadoService;
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
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public EmprestimoController(EmprestimoService emprestimoService,
                                UsuarioAutenticadoService usuarioAutenticadoService) {
        this.emprestimoService = emprestimoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USUARIO')")
    public EmprestimoResponse criar(@RequestBody @Valid CriarEmprestimoRequest request,
                                    Authentication authentication) {
        Long usuarioId = usuarioAutenticadoService.buscarId(authentication);
        return emprestimoService.criar(request, usuarioId);
    }

    @PutMapping("/{id}/devolver")
    @PreAuthorize("hasRole('ADMIN')")
    public EmprestimoResponse devolver(@PathVariable Long id, Authentication authentication) {
        Long adminId = usuarioAutenticadoService.buscarId(authentication);
        return emprestimoService.devolver(id, adminId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<EmprestimoResponse> listarEmprestimosDoAdmin(Authentication authentication) {
        Long adminId = usuarioAutenticadoService.buscarId(authentication);
        return emprestimoService.listarPorAdmin(adminId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USUARIO')")
    public List<EmprestimoResponse> listarMeusEmprestimos(Authentication authentication) {
        Long usuarioId = usuarioAutenticadoService.buscarId(authentication);
        return emprestimoService.listarPorUsuario(usuarioId);
    }
}