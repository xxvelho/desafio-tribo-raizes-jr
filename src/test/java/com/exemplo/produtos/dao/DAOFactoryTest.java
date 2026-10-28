package com.exemplo.produtos.dao;

import com.exemplo.produtos.model.Produto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
class DAOFactoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveCriarProdutoDAOJdbcQueUsaOJdbcTemplateRecebido() {
        ProdutoDAO dao = new DAOFactory(jdbcTemplate).criarProdutoDAO();

        Produto salvo = dao.salvar(new Produto(null, "Teclado", null, new BigDecimal("199.90"), 10));

        assertThat(dao).isInstanceOf(ProdutoDAOJdbc.class);
        assertThat(dao.buscarPorId(salvo.getId())).isPresent();
    }

}
