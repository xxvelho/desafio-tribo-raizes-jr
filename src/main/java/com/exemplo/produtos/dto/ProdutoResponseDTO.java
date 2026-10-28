package com.exemplo.produtos.dto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        int quantidadeEstoque
) {
}
