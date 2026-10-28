package com.exemplo.produtos.exception;

public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException(Long id) {
        super("Produto não encontrado com id " + id);
    }

}
