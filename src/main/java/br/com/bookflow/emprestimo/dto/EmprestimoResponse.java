package br.com.bookflow.emprestimo.dto;

import br.com.bookflow.emprestimo.entity.EmprestimoStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmprestimoResponse(

        Long id,

        Long usuarioId,
        String usuarioNome,
        String usuarioEmail,

        Long livroId,
        String tituloLivro,

        LocalDate dataEmprestimo,
        LocalDate dataPrevistaDevolucao,
        LocalDate dataDevolucao,

        BigDecimal valorEmprestimo,

        EmprestimoStatus status
) {
}