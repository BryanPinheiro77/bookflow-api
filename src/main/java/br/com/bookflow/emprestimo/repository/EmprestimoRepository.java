package br.com.bookflow.emprestimo.repository;

import br.com.bookflow.emprestimo.entity.Emprestimo;
import br.com.bookflow.emprestimo.entity.EmprestimoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findByUsuarioId(Long usuarioId);

    List<Emprestimo> findByLivroAdminId(Long adminId);

    boolean existsByLivroIdAndStatus(Long livroId, EmprestimoStatus status);
}