package br.com.bookflow.livro.dto;

import br.com.bookflow.livro.entity.LivroStatus;

public record LivroResponse(
        Long id,
        String titulo,
        String autor,
        String categoria,
        LivroStatus status,
        String capaUrl,
        Long adminId
) {
}