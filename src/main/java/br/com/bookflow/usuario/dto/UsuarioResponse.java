package br.com.bookflow.usuario.dto;

import br.com.bookflow.usuario.entity.Role;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Role role
) {
}