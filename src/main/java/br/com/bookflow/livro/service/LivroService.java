package br.com.bookflow.livro.service;

import br.com.bookflow.exception.PermissaoNegadaException;
import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.exception.RegraDeNegocioException;
import br.com.bookflow.livro.dto.AtualizarLivroRequest;
import br.com.bookflow.livro.dto.CadastrarLivroRequest;
import br.com.bookflow.livro.dto.LivroResponse;
import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.livro.entity.LivroStatus;
import br.com.bookflow.livro.repository.LivroRepository;
import br.com.bookflow.upload.dto.UploadResponse;
import br.com.bookflow.upload.service.UploadService;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final UploadService uploadService;

    public LivroService(LivroRepository livroRepository,
                        UsuarioRepository usuarioRepository,
                        UploadService uploadService) {
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.uploadService = uploadService;
    }

    public LivroResponse cadastrar(CadastrarLivroRequest request, Long adminId) {

        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Administrador não encontrado."
                        )
                );

        if (request.quantidadeDisponivel() > request.quantidadeTotal()) {
            throw new RegraDeNegocioException(
                    "Quantidade disponível não pode ser maior que a total."
            );
        }

        Livro livro = Livro.builder()
                .titulo(request.titulo())
                .autor(request.autor())
                .categoria(request.categoria())
                .quantidadeTotal(request.quantidadeTotal())
                .quantidadeDisponivel(request.quantidadeDisponivel())
                .valorEmprestimo(request.valorEmprestimo())
                .valorMultaDiaria(request.valorMultaDiaria())
                .capaUrl(request.capaUrl())
                .admin(admin)
                .build();

        atualizarStatusLivro(livro);

        Livro salvo = livroRepository.save(livro);

        return toResponse(salvo);
    }

    public List<LivroResponse> listarTodosParaUsuario() {
        return livroRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<LivroResponse> listarPorAdmin(Long adminId) {
        return livroRepository.findByAdminId(adminId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LivroResponse buscarPorIdParaUsuario(Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));

        return toResponse(livro);
    }

    public LivroResponse buscarPorIdParaAdmin(Long id, Long adminId) {
        Livro livro = livroRepository.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));

        return toResponse(livro);
    }

    public LivroResponse atualizar(Long id,
                                   AtualizarLivroRequest request,
                                   Long adminId) {

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Livro não encontrado."
                        )
                );

        if (!livro.getAdmin().getId().equals(adminId)) {
            throw new PermissaoNegadaException(
                    "Você não tem permissão para alterar este livro."
            );
        }

        if (request.quantidadeDisponivel() > request.quantidadeTotal()) {
            throw new RegraDeNegocioException(
                    "Quantidade disponível não pode ser maior que a total."
            );
        }

        livro.setTitulo(request.titulo());
        livro.setAutor(request.autor());
        livro.setCategoria(request.categoria());

        livro.setQuantidadeTotal(request.quantidadeTotal());
        livro.setQuantidadeDisponivel(request.quantidadeDisponivel());

        livro.setValorEmprestimo(request.valorEmprestimo());
        livro.setValorMultaDiaria(request.valorMultaDiaria());

        livro.setCapaUrl(request.capaUrl());

        atualizarStatusLivro(livro);

        Livro atualizado = livroRepository.save(livro);

        return toResponse(atualizado);
    }

    public void excluir(Long id, Long adminId) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));

        if (!livro.getAdmin().getId().equals(adminId)) {
            throw new PermissaoNegadaException("Você não tem permissão para alterar este livro.");
        }

        if (livro.getCapaUrl() != null && !livro.getCapaUrl().isBlank()) {
            uploadService.removerArquivo(livro.getCapaUrl());
        }

        livroRepository.delete(livro);
    }

    public LivroResponse uploadCapa(Long livroId, MultipartFile file, Long adminId) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));

        if (!livro.getAdmin().getId().equals(adminId)) {
            throw new PermissaoNegadaException("Você não tem permissão para alterar a capa deste livro.");
        }

        String capaAnterior = livro.getCapaUrl();

        UploadResponse uploadResponse = uploadService.uploadCapaLivro(file);

        livro.setCapaUrl(uploadResponse.fileUrl());

        Livro livroSalvo = livroRepository.save(livro);

        if (capaAnterior != null && !capaAnterior.isBlank()) {
            uploadService.removerArquivo(capaAnterior);
        }

        return toResponse(livroSalvo);
    }

    public LivroResponse removerCapa(Long livroId, Long adminId) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));

        if (!livro.getAdmin().getId().equals(adminId)) {
            throw new PermissaoNegadaException("Você não tem permissão para remover a capa deste livro.");
        }

        String capaAtual = livro.getCapaUrl();

        if (capaAtual != null && !capaAtual.isBlank()) {
            uploadService.removerArquivo(capaAtual);
        }

        livro.setCapaUrl(null);

        Livro livroSalvo = livroRepository.save(livro);
        return toResponse(livroSalvo);
    }

    private void atualizarStatusLivro(Livro livro) {

        if (livro.getQuantidadeDisponivel() == 0) {
            livro.setStatus(LivroStatus.INDISPONIVEL);
        } else {
            livro.setStatus(LivroStatus.DISPONIVEL);
        }
    }

    private LivroResponse toResponse(Livro livro) {

        return new LivroResponse(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getCategoria(),
                livro.getQuantidadeTotal(),
                livro.getQuantidadeDisponivel(),
                livro.getValorEmprestimo(),
                livro.getValorMultaDiaria(),
                livro.getStatus(),
                livro.getCapaUrl(),
                livro.getAdmin().getId()
        );
    }
}