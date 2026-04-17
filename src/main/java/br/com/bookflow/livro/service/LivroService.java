package br.com.bookflow.livro.service;

import br.com.bookflow.exception.PermissaoNegadaException;
import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.livro.dto.AtualizarLivroRequest;
import br.com.bookflow.livro.dto.CadastrarLivroRequest;
import br.com.bookflow.livro.dto.LivroResponse;
import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.livro.entity.LivroStatus;
import br.com.bookflow.livro.repository.LivroRepository;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public LivroService(LivroRepository livroRepository,
                        UsuarioRepository usuarioRepository) {
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public LivroResponse cadastrar(CadastrarLivroRequest request, Long adminId) {

        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Administrador não encontrado."));

        Livro livro = Livro.builder()
                .titulo(request.titulo())
                .autor(request.autor())
                .categoria(request.categoria())
                .status(LivroStatus.DISPONIVEL)
                .capaUrl(request.capaUrl())
                .admin(admin)
                .build();

        return toResponse(livroRepository.save(livro));
    }

    public List<LivroResponse> listarTodos() {
        return livroRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LivroResponse buscarPorId(Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Livro não encontrado."));

        return toResponse(livro);
    }

    public LivroResponse atualizar(Long id,
                                   AtualizarLivroRequest request,
                                   Long adminId) {

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Livro não encontrado."));

        validarPermissaoAdmin(livro, adminId);

        livro.setTitulo(request.titulo());
        livro.setAutor(request.autor());
        livro.setCategoria(request.categoria());
        livro.setCapaUrl(request.capaUrl());

        return toResponse(livroRepository.save(livro));
    }

    public void excluir(Long id, Long adminId) {

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Livro não encontrado."));

        validarPermissaoAdmin(livro, adminId);

        livroRepository.delete(livro);
    }

    private void validarPermissaoAdmin(Livro livro, Long adminId) {
        if (!livro.getAdmin().getId().equals(adminId)) {
            throw new PermissaoNegadaException(
                    "Você não tem permissão para alterar este livro."
            );
        }
    }

    private LivroResponse toResponse(Livro livro) {
        return new LivroResponse(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getCategoria(),
                livro.getStatus(),
                livro.getCapaUrl(),
                livro.getAdmin().getId()
        );
    }
}