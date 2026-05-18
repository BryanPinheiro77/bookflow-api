package br.com.bookflow.livro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record AtualizarLivroRequest(

        @NotBlank(message = "O título é obrigatório")
        String titulo,

        @NotBlank(message = "O autor é obrigatório")
        String autor,

        @NotBlank(message = "A categoria é obrigatória")
        String categoria,

        @Min(value = 1, message = "A quantidade total deve ser maior que zero")
        Integer quantidadeTotal,

        @Min(value = 0, message = "A quantidade disponível não pode ser negativa")
        Integer quantidadeDisponivel,

        @DecimalMin(value = "0.0", message = "O valor do empréstimo não pode ser negativo")
        BigDecimal valorEmprestimo,

        @DecimalMin(value = "0.0", message = "O valor da multa não pode ser negativo")
        BigDecimal valorMultaDiaria,

        String capaUrl
) {
}