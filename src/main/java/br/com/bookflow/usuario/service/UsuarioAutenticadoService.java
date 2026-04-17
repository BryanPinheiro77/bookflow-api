package br.com.bookflow.usuario.service;

import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario buscar(Authentication authentication) {
        String email = authentication.getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
    }

    public Long buscarId(Authentication authentication) {
        return buscar(authentication).getId();
    }

    public String buscarEmail(Authentication authentication) {
        return authentication.getName();
    }
}