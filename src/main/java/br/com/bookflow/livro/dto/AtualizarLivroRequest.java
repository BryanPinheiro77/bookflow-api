package br.com.bookflow.livro.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarLivroRequest(

        @NotBlank(message = "O título é obrigatório")
        String titulo,

        @NotBlank(message = "O autor é obrigatório")
        String autor,

        @NotBlank(message = "A categoria é obrigatória")
        String categoria,

        String capaUrl
) {
}