package com.exemplo.produtos.service;

import com.exemplo.produtos.dto.ProdutoRequestDTO;
import com.exemplo.produtos.dto.ProdutoResponseDTO;

import java.util.List;

public interface ProdutoService {

    ProdutoResponseDTO criar(ProdutoRequestDTO request);

    ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO request);

    void remover(Long id);

    ProdutoResponseDTO buscarPorId(Long id);

    List<ProdutoResponseDTO> listarTodos();

}
