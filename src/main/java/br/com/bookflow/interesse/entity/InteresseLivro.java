package br.com.bookflow.interesse.entity;

import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "interesses_livro",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"usuario_id", "livro_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteresseLivro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Column(name = "data_interesse", nullable = false)
    private LocalDateTime dataInteresse;
}