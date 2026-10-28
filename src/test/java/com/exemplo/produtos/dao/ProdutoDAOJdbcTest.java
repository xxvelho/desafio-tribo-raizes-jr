package com.exemplo.produtos.dao;

import com.exemplo.produtos.model.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
class ProdutoDAOJdbcTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ProdutoDAOJdbc dao;

    @BeforeEach
    void setUp() {
        dao = new ProdutoDAOJdbc(jdbcTemplate);
    }

    @Test
    void deveSalvarProdutoEGerarId() {
        Produto produto = novoProduto("Teclado", "Teclado mecânico", "199.90", 10);

        Produto salvo = dao.salvar(produto);

        assertThat(salvo.getId()).isNotNull();
    }

    @Test
    void deveBuscarProdutoSalvoPorId() {
        Produto salvo = dao.salvar(novoProduto("Teclado", "Teclado mecânico", "199.90", 10));

        Optional<Produto> encontrado = dao.buscarPorId(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Teclado");
        assertThat(encontrado.get().getDescricao()).isEqualTo("Teclado mecânico");
        assertThat(encontrado.get().getPreco()).isEqualByComparingTo("199.90");
        assertThat(encontrado.get().getQuantidadeEstoque()).isEqualTo(10);
    }

    @Test
    void deveRetornarVazioAoBuscarIdInexistente() {
        assertThat(dao.buscarPorId(999L)).isEmpty();
    }

    @Test
    void deveListarTodosOsProdutos() {
        dao.salvar(novoProduto("Teclado", null, "199.90", 10));
        dao.salvar(novoProduto("Mouse", null, "59.50", 0));

        List<Produto> produtos = dao.listarTodos();

        assertThat(produtos).extracting(Produto::getNome).containsExactly("Teclado", "Mouse");
    }

    @Test
    void deveAtualizarProdutoExistente() {
        Produto salvo = dao.salvar(novoProduto("Teclado", "Teclado mecânico", "199.90", 10));
        salvo.setNome("Teclado gamer");
        salvo.setPreco(new BigDecimal("249.90"));
        salvo.setQuantidadeEstoque(5);

        boolean atualizado = dao.atualizar(salvo);

        assertThat(atualizado).isTrue();
        Produto recarregado = dao.buscarPorId(salvo.getId()).orElseThrow();
        assertThat(recarregado.getNome()).isEqualTo("Teclado gamer");
        assertThat(recarregado.getPreco()).isEqualByComparingTo("249.90");
        assertThat(recarregado.getQuantidadeEstoque()).isEqualTo(5);
    }

    @Test
    void naoDeveAtualizarProdutoInexistente() {
        Produto produto = novoProduto("Fantasma", null, "10.00", 1);
        produto.setId(999L);

        assertThat(dao.atualizar(produto)).isFalse();
    }

    @Test
    void deveRemoverProdutoExistente() {
        Produto salvo = dao.salvar(novoProduto("Teclado", null, "199.90", 10));

        boolean removido = dao.remover(salvo.getId());

        assertThat(removido).isTrue();
        assertThat(dao.buscarPorId(salvo.getId())).isEmpty();
    }

    @Test
    void naoDeveRemoverProdutoInexistente() {
        assertThat(dao.remover(999L)).isFalse();
    }

    private Produto novoProduto(String nome, String descricao, String preco, int quantidadeEstoque) {
        return new Produto(null, nome, descricao, new BigDecimal(preco), quantidadeEstoque);
    }

}
