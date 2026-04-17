package br.com.bookflow.interesse.service;

import br.com.bookflow.exception.RegraDeNegocioException;
import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.interesse.dto.InteresseLivroResponse;
import br.com.bookflow.interesse.entity.InteresseLivro;
import br.com.bookflow.interesse.repository.InteresseLivroRepository;
import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.livro.entity.LivroStatus;
import br.com.bookflow.livro.repository.LivroRepository;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InteresseLivroService {

    private final InteresseLivroRepository interesseLivroRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public InteresseLivroService(
            InteresseLivroRepository interesseLivroRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.interesseLivroRepository = interesseLivroRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public InteresseLivroResponse registrarInteresse(Long usuarioId, Long livroId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));

        if (livro.getStatus() != LivroStatus.EMPRESTADO) {
            throw new RegraDeNegocioException("Só é possível registrar interesse em livros emprestados.");
        }

        boolean jaExisteInteresse = interesseLivroRepository.existsByUsuarioIdAndLivroId(usuarioId, livroId);
        if (jaExisteInteresse) {
            throw new RegraDeNegocioException("Você já registrou interesse neste livro.");
        }

        InteresseLivro interesse = new InteresseLivro();
        interesse.setUsuario(usuario);
        interesse.setLivro(livro);
        interesse.setDataInteresse(LocalDateTime.now());

        InteresseLivro salvo = interesseLivroRepository.save(interesse);

        return toResponse(salvo);
    }

    public List<InteresseLivroResponse> listarMeusInteresses(Long usuarioId) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        return interesseLivroRepository.findByUsuarioIdOrderByDataInteresseDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void removerInteresse(Long usuarioId, Long livroId) {
        InteresseLivro interesse = interesseLivroRepository.findByUsuarioIdAndLivroId(usuarioId, livroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Interesse não encontrado para este livro."));

        interesseLivroRepository.delete(interesse);
    }

    private InteresseLivroResponse toResponse(InteresseLivro interesse) {
        return new InteresseLivroResponse(
                interesse.getId(),
                interesse.getLivro().getId(),
                interesse.getLivro().getTitulo(),
                interesse.getLivro().getAutor(),
                interesse.getDataInteresse()
        );
    }
}