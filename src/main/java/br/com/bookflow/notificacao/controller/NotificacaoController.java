package br.com.bookflow.notificacao.controller;

import br.com.bookflow.notificacao.dto.NotificacaoResponse;
import br.com.bookflow.notificacao.service.NotificacaoService;
import br.com.bookflow.usuario.service.UsuarioAutenticadoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public NotificacaoController(NotificacaoService notificacaoService,
                                 UsuarioAutenticadoService usuarioAutenticadoService) {
        this.notificacaoService = notificacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public List<NotificacaoResponse> listarMinhas(Authentication authentication) {
        Long usuarioId = usuarioAutenticadoService.buscarId(authentication);
        return notificacaoService.listarMinhasNotificacoes(usuarioId);
    }

    @PatchMapping("/{id}/ler")
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public void marcarComoLida(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = usuarioAutenticadoService.buscarId(authentication);
        notificacaoService.marcarComoLida(id, usuarioId);
    }
}