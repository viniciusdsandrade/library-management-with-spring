package com.restful.challange.library.api.service.impl;

import com.restful.challange.library.api.dto.autor.DadosCadastroAutor;
import com.restful.challange.library.api.dto.autor.DadosListagemAutor;
import com.restful.challange.library.api.entity.Autor;
import com.restful.challange.library.api.entity.Livro;
import com.restful.challange.library.api.exception.DuplicateEntryException;
import com.restful.challange.library.api.exception.ValidacaoException;
import com.restful.challange.library.api.repository.AutorRepository;
import com.restful.challange.library.api.service.AutorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class AutorServiceImpl implements AutorService {

    private final AutorRepository autorRepository;

    public AutorServiceImpl(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    @Override
    @Transactional
    public Autor save(DadosCadastroAutor dadosCadastroAutor) {

        //Validamos o CPF e a data de nascimento
        validarDadosCadastroAutor(dadosCadastroAutor);

        // Cria um novo Autor
        Autor autor = new Autor(dadosCadastroAutor);

        // Se existirem livros, adiciona cada um deles ao autor
        Optional.ofNullable(dadosCadastroAutor.livros())
                .ifPresent(livros -> livros.forEach(livro -> livro.addAutor(autor)));

        // Salva o autor com os livros associados
        return autorRepository.save(autor);
    }

    @Override
    public Page<DadosListagemAutor> listar(Pageable pageable) {
        return autorRepository.findAll(pageable).map(DadosListagemAutor::new);
    }

    @Override
    public Autor buscarPorId(Long id) {
        return autorRepository.findById(id).orElseThrow(() -> new ValidacaoException("Autor não encontrado"));
    }

    @Override
    public Page<DadosListagemAutor> buscarPorNome(String nome, Pageable paginacao) {
        return autorRepository.findByNomeContaining(nome, paginacao).map(DadosListagemAutor::new);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Autor autor = autorRepository.findById(id).orElseThrow(() -> new ValidacaoException("Autor não encontrado"));
        autorRepository.delete(autor);
    }

    private void validarDadosAtualizacaoAutor(DadosCadastroAutor dadosCadastroAutor) {
        LocalDate dataNascimento = LocalDate.parse(dadosCadastroAutor.nascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        validarDataNascimento(dataNascimento);
        validarIdadeMinima(dataNascimento);
    }

    private void validarDadosCadastroAutor(DadosCadastroAutor dadosCadastroAutor) {
        String cpf = dadosCadastroAutor.cpf().replaceAll("[.\\-]", "");
        LocalDate dataNascimento = LocalDate.parse(dadosCadastroAutor.nascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        validarDataNascimento(dataNascimento);
        validarIdadeMinima(dataNascimento);
        validarCpfUnico(cpf);
    }

    private void validarDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new ValidacaoException("A data de nascimento não pode ser posterior à data atual");
        }
    }

    private void validarIdadeMinima(LocalDate dataNascimento) {
        if (dataNascimento.plusYears(14).isAfter(LocalDate.now())) {
            throw new ValidacaoException("O autor deve ter no mínimo 14 anos de idade");
        }
    }

    private void validarCpfUnico(String cpf) {
        if (autorRepository.existsByCPF(cpf)) {
            throw new DuplicateEntryException("Já existe um autor com o cpf " + cpf);
        }
    }
}
