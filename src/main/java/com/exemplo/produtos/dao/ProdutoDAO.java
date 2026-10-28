package com.exemplo.produtos.dao;

import com.exemplo.produtos.model.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoDAO {

    Produto salvar(Produto produto);

    boolean atualizar(Produto produto);

    boolean remover(Long id);

    Optional<Produto> buscarPorId(Long id);

    List<Produto> listarTodos();

}
