package br.com.bookflow.notificacao.repository;

import br.com.bookflow.notificacao.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByDestinatarioIdOrderByDataCriacaoDesc(Long destinatarioId);

    Optional<Notificacao> findByIdAndDestinatarioId(Long notificacaoId, Long destinatarioId);

    long countByDestinatarioIdAndLidaFalse(Long destinatarioId);

    List<Notificacao> findByDestinatarioIdAndLidaFalse(Long destinatarioId);
}