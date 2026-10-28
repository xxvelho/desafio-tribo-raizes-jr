package com.exemplo.produtos.dao;

import com.exemplo.produtos.model.Produto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class ProdutoDAOJdbc implements ProdutoDAO {

    private static final String INSERT =
            "INSERT INTO produto (nome, descricao, preco, quantidade_estoque) VALUES (?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE produto SET nome = ?, descricao = ?, preco = ?, quantidade_estoque = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM produto WHERE id = ?";
    private static final String SELECT_POR_ID =
            "SELECT id, nome, descricao, preco, quantidade_estoque FROM produto WHERE id = ?";
    private static final String SELECT_TODOS =
            "SELECT id, nome, descricao, preco, quantidade_estoque FROM produto ORDER BY id";

    private final JdbcTemplate jdbcTemplate;

    public ProdutoDAOJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Produto salvar(Produto produto) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getDescricao());
            ps.setBigDecimal(3, produto.getPreco());
            ps.setInt(4, produto.getQuantidadeEstoque());
            return ps;
        }, keyHolder);
        produto.setId(keyHolder.getKey().longValue());
        return produto;
    }

    @Override
    public boolean atualizar(Produto produto) {
        int linhasAfetadas = jdbcTemplate.update(UPDATE,
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidadeEstoque(),
                produto.getId());
        return linhasAfetadas > 0;
    }

    @Override
    public boolean remover(Long id) {
        return jdbcTemplate.update(DELETE, id) > 0;
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return jdbcTemplate.query(SELECT_POR_ID, this::mapearProduto, id).stream().findFirst();
    }

    @Override
    public List<Produto> listarTodos() {
        return jdbcTemplate.query(SELECT_TODOS, this::mapearProduto);
    }

    private Produto mapearProduto(ResultSet rs, int numeroLinha) throws SQLException {
        return new Produto(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getBigDecimal("preco"),
                rs.getInt("quantidade_estoque"));
    }

}
