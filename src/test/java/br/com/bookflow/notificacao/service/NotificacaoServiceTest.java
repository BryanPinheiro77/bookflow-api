package br.com.bookflow.notificacao.service;

import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.notificacao.dto.NotificacaoResponse;
import br.com.bookflow.notificacao.dto.QuantidadeNotificacoesNaoLidasResponse;
import br.com.bookflow.notificacao.entity.Notificacao;
import br.com.bookflow.notificacao.repository.NotificacaoRepository;
import br.com.bookflow.usuario.entity.Role;
import br.com.bookflow.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificacaoServiceTest {

    private NotificacaoRepository notificacaoRepository;
    private NotificacaoService notificacaoService;

    @BeforeEach
    void setUp() {
        notificacaoRepository = mock(NotificacaoRepository.class);
        notificacaoService = new NotificacaoService(notificacaoRepository);
    }

    @Test
    void deveCriarNotificacaoComSucesso() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Bryan")
                .email("bryan@email.com")
                .senha("123456")
                .role(Role.USUARIO)
                .build();

        notificacaoService.criarNotificacao(
                usuario,
                "Livro disponível novamente",
                "O livro \"Clean Code\" está disponível para empréstimo novamente."
        );

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(captor.capture());

        Notificacao notificacaoSalva = captor.getValue();

        assertEquals(usuario, notificacaoSalva.getDestinatario());
        assertEquals("Livro disponível novamente", notificacaoSalva.getTitulo());
        assertEquals("O livro \"Clean Code\" está disponível para empréstimo novamente.", notificacaoSalva.getMensagem());
        assertFalse(notificacaoSalva.getLida());
        assertNotNull(notificacaoSalva.getDataCriacao());
    }

    @Test
    void deveListarMinhasNotificacoes() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .role(Role.USUARIO)
                .build();

        Notificacao notificacao1 = Notificacao.builder()
                .id(10L)
                .titulo("Livro disponível novamente")
                .mensagem("O livro \"Clean Code\" está disponível para empréstimo novamente.")
                .lida(false)
                .dataCriacao(LocalDateTime.now().minusHours(2))
                .destinatario(usuario)
                .build();

        Notificacao notificacao2 = Notificacao.builder()
                .id(11L)
                .titulo("Livro disponível novamente")
                .mensagem("O livro \"DDD\" está disponível para empréstimo novamente.")
                .lida(true)
                .dataCriacao(LocalDateTime.now().minusHours(1))
                .destinatario(usuario)
                .build();

        when(notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of(notificacao2, notificacao1));

        List<NotificacaoResponse> response = notificacaoService.listarMinhasNotificacoes(1L);

        assertEquals(2, response.size());
        assertEquals(11L, response.get(0).id());
        assertEquals(10L, response.get(1).id());
    }

    @Test
    void deveMarcarNotificacaoComoLidaComSucesso() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .role(Role.USUARIO)
                .build();

        Notificacao notificacao = Notificacao.builder()
                .id(10L)
                .titulo("Livro disponível novamente")
                .mensagem("Mensagem")
                .lida(false)
                .dataCriacao(LocalDateTime.now())
                .destinatario(usuario)
                .build();

        when(notificacaoRepository.findByIdAndDestinatarioId(10L, 1L))
                .thenReturn(Optional.of(notificacao));

        notificacaoService.marcarComoLida(10L, 1L);

        assertTrue(notificacao.getLida());
        verify(notificacaoRepository).save(notificacao);
    }

    @Test
    void deveLancarExcecaoQuandoNotificacaoNaoForEncontradaAoMarcarComoLida() {
        when(notificacaoRepository.findByIdAndDestinatarioId(10L, 1L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> notificacaoService.marcarComoLida(10L, 1L)
        );

        assertEquals("Notificação não encontrada.", exception.getMessage());
        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }

    @Test
    void deveMarcarTodasComoLidasComSucesso() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .role(Role.USUARIO)
                .build();

        Notificacao notificacao1 = Notificacao.builder()
                .id(10L)
                .titulo("N1")
                .mensagem("M1")
                .lida(false)
                .dataCriacao(LocalDateTime.now().minusHours(2))
                .destinatario(usuario)
                .build();

        Notificacao notificacao2 = Notificacao.builder()
                .id(11L)
                .titulo("N2")
                .mensagem("M2")
                .lida(false)
                .dataCriacao(LocalDateTime.now().minusHours(1))
                .destinatario(usuario)
                .build();

        when(notificacaoRepository.findByDestinatarioIdAndLidaFalse(1L))
                .thenReturn(List.of(notificacao1, notificacao2));

        notificacaoService.marcarTodasComoLidas(1L);

        assertTrue(notificacao1.getLida());
        assertTrue(notificacao2.getLida());
        verify(notificacaoRepository).saveAll(List.of(notificacao1, notificacao2));
    }

    @Test
    void naoDeveSalvarNadaQuandoNaoHouverNotificacoesNaoLidasAoMarcarTodasComoLidas() {
        when(notificacaoRepository.findByDestinatarioIdAndLidaFalse(1L))
                .thenReturn(List.of());

        notificacaoService.marcarTodasComoLidas(1L);

        verify(notificacaoRepository, never()).saveAll(anyList());
    }

    @Test
    void deveContarNotificacoesNaoLidas() {
        when(notificacaoRepository.countByDestinatarioIdAndLidaFalse(1L))
                .thenReturn(3L);

        QuantidadeNotificacoesNaoLidasResponse response = notificacaoService.contarNaoLidas(1L);

        assertEquals(3L, response.quantidade());
    }
}