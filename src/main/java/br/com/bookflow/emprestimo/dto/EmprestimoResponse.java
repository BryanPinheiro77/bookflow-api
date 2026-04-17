package br.com.bookflow.emprestimo.dto;

import br.com.bookflow.emprestimo.entity.EmprestimoStatus;

import java.time.LocalDate;

public record EmprestimoResponse(
        Long id,
        Long usuarioId,
        Long livroId,
        String tituloLivro,
        LocalDate dataEmprestimo,
        LocalDate dataDevolucao,
        EmprestimoStatus status
) {
}