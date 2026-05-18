package br.com.bookflow.livro.dto;

import br.com.bookflow.livro.entity.LivroStatus;

import java.math.BigDecimal;

public record LivroResponse(

        Long id,
        String titulo,
        String autor,
        String categoria,
        Integer quantidadeTotal,
        Integer quantidadeDisponivel,
        BigDecimal valorEmprestimo,
        BigDecimal valorMultaDiaria,
        LivroStatus status,
        String capaUrl,
        Long adminId
) {
}