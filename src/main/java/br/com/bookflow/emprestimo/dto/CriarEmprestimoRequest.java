package br.com.bookflow.emprestimo.dto;

import jakarta.validation.constraints.NotNull;

public record CriarEmprestimoRequest(
        @NotNull(message = "O id do livro é obrigatório")
        Long livroId
) {
}