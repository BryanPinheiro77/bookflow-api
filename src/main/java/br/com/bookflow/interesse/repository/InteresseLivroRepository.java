package br.com.bookflow.interesse.repository;

import br.com.bookflow.interesse.entity.InteresseLivro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InteresseLivroRepository extends JpaRepository<InteresseLivro, Long> {

    boolean existsByUsuarioIdAndLivroId(Long usuarioId, Long livroId);

    List<InteresseLivro> findByUsuarioIdOrderByDataInteresseDesc(Long usuarioId);

    List<InteresseLivro> findByLivroId(Long livroId);

    Optional<InteresseLivro> findByUsuarioIdAndLivroId(Long usuarioId, Long livroId);

    void deleteByLivroId(Long livroId);
}