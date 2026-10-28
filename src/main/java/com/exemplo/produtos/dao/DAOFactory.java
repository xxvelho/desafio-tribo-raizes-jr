package com.exemplo.produtos.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DAOFactory {

    private final JdbcTemplate jdbcTemplate;

    public DAOFactory(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ProdutoDAO criarProdutoDAO() {
        return new ProdutoDAOJdbc(jdbcTemplate);
    }

}
