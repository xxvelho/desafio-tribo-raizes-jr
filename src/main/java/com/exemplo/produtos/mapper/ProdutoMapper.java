package com.exemplo.produtos.mapper;

import com.exemplo.produtos.dto.ProdutoRequestDTO;
import com.exemplo.produtos.dto.ProdutoResponseDTO;
import com.exemplo.produtos.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoMapper {

    public Produto toEntity(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        // a coluna tem 2 casas decimais; assim POST e PUT respondem no mesmo formato do GET
        produto.setPreco(dto.preco().setScale(2));
        produto.setQuantidadeEstoque(dto.quantidadeEstoque());
        return produto;
    }

    public ProdutoResponseDTO toResponse(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidadeEstoque());
    }

}
