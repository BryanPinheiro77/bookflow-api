package br.com.bookflow.livro.entity;

import br.com.bookflow.usuario.entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "livros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(nullable = false)
    private String categoria;

    @Min(0)
    @Column(nullable = false)
    private Integer quantidadeTotal;

    @Min(0)
    @Column(nullable = false)
    private Integer quantidadeDisponivel;

    @DecimalMin("0.0")
    @Column(nullable = false)
    private BigDecimal valorEmprestimo;

    @DecimalMin("0.0")
    @Column(nullable = false)
    private BigDecimal valorMultaDiaria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LivroStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Usuario admin;

    @Column(name = "capa_url")
    private String capaUrl;
}