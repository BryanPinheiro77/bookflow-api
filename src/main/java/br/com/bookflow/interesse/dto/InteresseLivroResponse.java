package br.com.bookflow.interesse.dto;

import java.time.LocalDateTime;

public record InteresseLivroResponse(
        Long id,
        Long livroId,
        String tituloLivro,
        String autorLivro,
        LocalDateTime dataInteresse
) {
}