package com.restful.challange.library.api.dto.autor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.restful.challange.library.api.dto.livro.DadosDetalhamentoLivro;
import com.restful.challange.library.api.entity.Autor;
import com.restful.challange.library.api.entity.Sexo;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record DadosListagemAutor(
        Long id,
        String nome,
        Sexo sexo,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate nascimento,
        String cpf,
        List<DadosDetalhamentoLivro> livros
) {

    public DadosListagemAutor(Autor autor) {
        this(
                autor.getId(),
                autor.getNome(),
                autor.getSexo(),
                autor.getNascimento(),
                autor.getCPF(),
                autor.getLivros().stream()
                        .map(DadosDetalhamentoLivro::new)
                        .collect(Collectors.toList())
        );
    }
}
