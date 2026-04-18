package br.com.bookflow.livro.repository;

import br.com.bookflow.livro.entity.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    List<Livro> findByAdminId(Long adminId);

    Optional<Livro> findByIdAndAdminId(Long id, Long adminId);
}