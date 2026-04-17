package br.com.bookflow.interesse.entity;

import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.usuario.entity.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "interesses_livro",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"usuario_id", "livro_id"})
        }
)
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

    public InteresseLivro() {
    }

    public InteresseLivro(Usuario usuario, Livro livro, LocalDateTime dataInteresse) {
        this.usuario = usuario;
        this.livro = livro;
        this.dataInteresse = dataInteresse;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public LocalDateTime getDataInteresse() {
        return dataInteresse;
    }

    public void setDataInteresse(LocalDateTime dataInteresse) {
        this.dataInteresse = dataInteresse;
    }
}