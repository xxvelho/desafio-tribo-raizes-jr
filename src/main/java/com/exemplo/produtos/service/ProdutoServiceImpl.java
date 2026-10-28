package com.exemplo.produtos.service;

import com.exemplo.produtos.dao.DAOFactory;
import com.exemplo.produtos.dao.ProdutoDAO;
import com.exemplo.produtos.dto.ProdutoRequestDTO;
import com.exemplo.produtos.dto.ProdutoResponseDTO;
import com.exemplo.produtos.exception.ProdutoNaoEncontradoException;
import com.exemplo.produtos.mapper.ProdutoMapper;
import com.exemplo.produtos.model.Produto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoDAO produtoDAO;
    private final ProdutoMapper mapper;

    public ProdutoServiceImpl(DAOFactory daoFactory, ProdutoMapper mapper) {
        this.produtoDAO = daoFactory.criarProdutoDAO();
        this.mapper = mapper;
    }

    @Override
    public ProdutoResponseDTO criar(ProdutoRequestDTO request) {
        Produto produto = mapper.toEntity(request);
        return mapper.toResponse(produtoDAO.salvar(produto));
    }

    @Override
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO request) {
        Produto produto = mapper.toEntity(request);
        produto.setId(id);
        if (!produtoDAO.atualizar(produto)) {
            throw new ProdutoNaoEncontradoException(id);
        }
        return mapper.toResponse(produto);
    }

    @Override
    public void remover(Long id) {
        if (!produtoDAO.remover(id)) {
            throw new ProdutoNaoEncontradoException(id);
        }
    }

    @Override
    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = produtoDAO.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
        return mapper.toResponse(produto);
    }

    @Override
    public List<ProdutoResponseDTO> listarTodos() {
        return produtoDAO.listarTodos().stream()
                .map(mapper::toResponse)
                .toList();
    }

}
