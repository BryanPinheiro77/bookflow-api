package br.com.bookflow.emprestimo.service;

import br.com.bookflow.emprestimo.dto.CriarEmprestimoRequest;
import br.com.bookflow.emprestimo.dto.EmprestimoResponse;
import br.com.bookflow.emprestimo.entity.Emprestimo;
import br.com.bookflow.emprestimo.entity.EmprestimoStatus;
import br.com.bookflow.emprestimo.repository.EmprestimoRepository;
import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.livro.entity.LivroStatus;
import br.com.bookflow.livro.repository.LivroRepository;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             LivroRepository livroRepository,
                             UsuarioRepository usuarioRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public EmprestimoResponse criar(CriarEmprestimoRequest request, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Livro livro = livroRepository.findById(request.livroId())
                .orElseThrow(() -> new RuntimeException("Livro não encontrado."));

        if (livro.getStatus() != LivroStatus.DISPONIVEL) {
            throw new RuntimeException("O livro não está disponível para empréstimo.");
        }

        if (emprestimoRepository.existsByLivroIdAndStatus(livro.getId(), EmprestimoStatus.ATIVO)) {
            throw new RuntimeException("Já existe um empréstimo ativo para este livro.");
        }

        Emprestimo emprestimo = Emprestimo.builder()
                .usuario(usuario)
                .livro(livro)
                .dataEmprestimo(LocalDate.now())
                .status(EmprestimoStatus.ATIVO)
                .build();

        livro.setStatus(LivroStatus.EMPRESTADO);

        Emprestimo emprestimoSalvo = emprestimoRepository.save(emprestimo);
        livroRepository.save(livro);

        return toResponse(emprestimoSalvo);
    }

    @Transactional
    public EmprestimoResponse devolver(Long emprestimoId, Long adminId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado."));

        if (!emprestimo.getLivro().getAdmin().getId().equals(adminId)) {
            throw new RuntimeException("Você não tem permissão para devolver este empréstimo.");
        }

        if (emprestimo.getStatus() == EmprestimoStatus.FINALIZADO) {
            throw new RuntimeException("Este empréstimo já foi finalizado.");
        }

        emprestimo.setStatus(EmprestimoStatus.FINALIZADO);
        emprestimo.setDataDevolucao(LocalDate.now());

        Livro livro = emprestimo.getLivro();
        livro.setStatus(LivroStatus.DISPONIVEL);

        Emprestimo emprestimoAtualizado = emprestimoRepository.save(emprestimo);
        livroRepository.save(livro);

        return toResponse(emprestimoAtualizado);
    }

    public List<EmprestimoResponse> listarPorAdmin(Long adminId) {
        return emprestimoRepository.findByLivroAdminId(adminId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EmprestimoResponse> listarPorUsuario(Long usuarioId) {
        return emprestimoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EmprestimoResponse toResponse(Emprestimo emprestimo) {
        return new EmprestimoResponse(
                emprestimo.getId(),
                emprestimo.getUsuario().getId(),
                emprestimo.getLivro().getId(),
                emprestimo.getLivro().getTitulo(),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataDevolucao(),
                emprestimo.getStatus()
        );
    }
}