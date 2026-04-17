package br.com.bookflow.notificacao.service;

import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.notificacao.dto.NotificacaoResponse;
import br.com.bookflow.notificacao.entity.Notificacao;
import br.com.bookflow.notificacao.repository.NotificacaoRepository;
import br.com.bookflow.usuario.entity.Usuario;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    public void criarNotificacao(Usuario destinatario, String titulo, String mensagem) {
        Notificacao notificacao = Notificacao.builder()
                .destinatario(destinatario)
                .titulo(titulo)
                .mensagem(mensagem)
                .lida(false)
                .dataCriacao(LocalDateTime.now())
                .build();

        notificacaoRepository.save(notificacao);
    }

    public List<NotificacaoResponse> listarMinhasNotificacoes(Long usuarioId) {
        return notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void marcarComoLida(Long notificacaoId, Long usuarioId) {
        Notificacao notificacao = notificacaoRepository.findByIdAndDestinatarioId(notificacaoId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada."));

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    private NotificacaoResponse toResponse(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                notificacao.getLida(),
                notificacao.getDataCriacao()
        );
    }
}