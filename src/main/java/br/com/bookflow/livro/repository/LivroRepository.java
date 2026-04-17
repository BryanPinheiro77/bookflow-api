package br.com.bookflow.livro.repository;

import br.com.bookflow.livro.entity.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {
}