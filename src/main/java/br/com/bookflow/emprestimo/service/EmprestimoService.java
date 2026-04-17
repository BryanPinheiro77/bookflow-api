package br.com.bookflow.emprestimo.service;

import br.com.bookflow.emprestimo.dto.CriarEmprestimoRequest;
import br.com.bookflow.emprestimo.dto.EmprestimoResponse;
import br.com.bookflow.emprestimo.entity.Emprestimo;
import br.com.bookflow.emprestimo.entity.EmprestimoStatus;
import br.com.bookflow.emprestimo.repository.EmprestimoRepository;
import br.com.bookflow.exception.PermissaoNegadaException;
import br.com.bookflow.exception.RecursoNaoEncontradoException;
import br.com.bookflow.exception.RegraDeNegocioException;
import br.com.bookflow.interesse.entity.InteresseLivro;
import br.com.bookflow.interesse.repository.InteresseLivroRepository;
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
    private final InteresseLivroRepository interesseLivroRepository;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             LivroRepository livroRepository,
                             UsuarioRepository usuarioRepository,
                             InteresseLivroRepository interesseLivroRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.interesseLivroRepository = interesseLivroRepository;
    }

    @Transactional
    public EmprestimoResponse criar(CriarEmprestimoRequest request, Long usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Usuário não encontrado."));

        Livro livro = livroRepository.findById(request.livroId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Livro não encontrado."));

        if (livro.getStatus() != LivroStatus.DISPONIVEL) {
            throw new RegraDeNegocioException(
                    "O livro não está disponível para empréstimo."
            );
        }

        if (emprestimoRepository.existsByLivroIdAndStatus(
                livro.getId(), EmprestimoStatus.ATIVO)) {

            throw new RegraDeNegocioException(
                    "Já existe um empréstimo ativo para este livro."
            );
        }

        Emprestimo emprestimo = Emprestimo.builder()
                .usuario(usuario)
                .livro(livro)
                .dataEmprestimo(LocalDate.now())
                .status(EmprestimoStatus.ATIVO)
                .build();

        livro.setStatus(LivroStatus.EMPRESTADO);

        Emprestimo salvo = emprestimoRepository.save(emprestimo);
        livroRepository.save(livro);

        return toResponse(salvo);
    }

    @Transactional
    public EmprestimoResponse devolver(Long emprestimoId, Long adminId) {

        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Empréstimo não encontrado."));

        if (!emprestimo.getLivro().getAdmin().getId().equals(adminId)) {
            throw new PermissaoNegadaException(
                    "Você não tem permissão para devolver este empréstimo."
            );
        }

        if (emprestimo.getStatus() == EmprestimoStatus.FINALIZADO) {
            throw new RegraDeNegocioException(
                    "Este empréstimo já foi finalizado."
            );
        }

        emprestimo.setStatus(EmprestimoStatus.FINALIZADO);
        emprestimo.setDataDevolucao(LocalDate.now());

        Livro livro = emprestimo.getLivro();
        livro.setStatus(LivroStatus.DISPONIVEL);

        Emprestimo salvo = emprestimoRepository.save(emprestimo);
        livroRepository.save(livro);

        List<InteresseLivro> interesses = interesseLivroRepository.findByLivroId(livro.getId());

        processarInteressesNaDevolucao(livro, interesses);

        return toResponse(salvo);
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

    private void processarInteressesNaDevolucao(Livro livro, List<InteresseLivro> interesses) {
        if (interesses.isEmpty()) {
            return;
        }

        // Próxima etapa:
        // 1. Criar notificações para os usuários interessados
        // 2. Decidir se os interesses serão mantidos ou removidos após o aviso
        // 3. Talvez ordenar/priorizar por dataInteresse, se necessário
    }

    private EmprestimoResponse toResponse(Emprestimo e) {
        return new EmprestimoResponse(
                e.getId(),
                e.getUsuario().getId(),
                e.getLivro().getId(),
                e.getLivro().getTitulo(),
                e.getDataEmprestimo(),
                e.getDataDevolucao(),
                e.getStatus()
        );
    }
}