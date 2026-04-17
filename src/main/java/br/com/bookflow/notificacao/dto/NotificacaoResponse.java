package br.com.bookflow.notificacao.dto;

import java.time.LocalDateTime;

public record NotificacaoResponse(
        Long id,
        String titulo,
        String mensagem,
        Boolean lida,
        LocalDateTime dataCriacao
) {
}