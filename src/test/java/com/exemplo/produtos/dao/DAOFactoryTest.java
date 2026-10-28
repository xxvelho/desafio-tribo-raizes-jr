package com.exemplo.produtos.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DAOFactoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveCriarProdutoDAOJdbc() {
        DAOFactory factory = new DAOFactory(jdbcTemplate);

        ProdutoDAO dao = factory.criarProdutoDAO();

        assertThat(dao).isInstanceOf(ProdutoDAOJdbc.class);
    }

}
