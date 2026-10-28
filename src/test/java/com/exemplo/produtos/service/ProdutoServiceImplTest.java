package com.exemplo.produtos.service;

import com.exemplo.produtos.dao.DAOFactory;
import com.exemplo.produtos.dao.ProdutoDAO;
import com.exemplo.produtos.dto.ProdutoRequestDTO;
import com.exemplo.produtos.dto.ProdutoResponseDTO;
import com.exemplo.produtos.exception.ProdutoNaoEncontradoException;
import com.exemplo.produtos.mapper.ProdutoMapper;
import com.exemplo.produtos.model.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceImplTest {

    @Mock
    private DAOFactory daoFactory;

    @Mock
    private ProdutoDAO produtoDAO;

    private ProdutoServiceImpl service;

    @BeforeEach
    void setUp() {
        when(daoFactory.criarProdutoDAO()).thenReturn(produtoDAO);
        service = new ProdutoServiceImpl(daoFactory, new ProdutoMapper());
    }

    @Test
    void deveCriarProdutoERetornarComId() {
        when(produtoDAO.salvar(any(Produto.class))).thenAnswer(invocation -> {
            Produto produto = invocation.getArgument(0);
            produto.setId(1L);
            return produto;
        });

        ProdutoResponseDTO response = service.criar(novaRequest());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Teclado");
        assertThat(response.descricao()).isEqualTo("Teclado mecânico");
        assertThat(response.preco()).isEqualByComparingTo("199.90");
        assertThat(response.quantidadeEstoque()).isEqualTo(10);

        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoDAO).salvar(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Teclado");
    }

    @Test
    void deveNormalizarPrecoParaDuasCasasDecimaisAoCriar() {
        when(produtoDAO.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ProdutoRequestDTO request = new ProdutoRequestDTO("Cabo", null, new BigDecimal("10"), 1);

        ProdutoResponseDTO response = service.criar(request);

        assertThat(response.preco()).hasToString("10.00");
    }

    @Test
    void deveAtualizarProdutoExistente() {
        when(produtoDAO.atualizar(any(Produto.class))).thenReturn(true);

        ProdutoResponseDTO response = service.atualizar(5L, novaRequest());

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.nome()).isEqualTo("Teclado");

        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoDAO).atualizar(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(5L);
    }

    @Test
    void deveNormalizarPrecoParaDuasCasasDecimaisAoAtualizar() {
        when(produtoDAO.atualizar(any(Produto.class))).thenReturn(true);
        ProdutoRequestDTO request = new ProdutoRequestDTO("Cabo", null, new BigDecimal("10"), 1);

        ProdutoResponseDTO response = service.atualizar(1L, request);

        assertThat(response.preco()).hasToString("10.00");
    }

    @Test
    void deveLancarExcecaoAoAtualizarProdutoInexistente() {
        when(produtoDAO.atualizar(any(Produto.class))).thenReturn(false);

        assertThatThrownBy(() -> service.atualizar(99L, novaRequest()))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado com id 99");
    }

    @Test
    void deveRemoverProdutoExistente() {
        when(produtoDAO.remover(1L)).thenReturn(true);

        service.remover(1L);

        verify(produtoDAO).remover(1L);
    }

    @Test
    void deveLancarExcecaoAoRemoverProdutoInexistente() {
        when(produtoDAO.remover(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.remover(99L))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado com id 99");
    }

    @Test
    void deveBuscarProdutoPorId() {
        Produto produto = new Produto(1L, "Mouse", null, new BigDecimal("59.50"), 3);
        when(produtoDAO.buscarPorId(1L)).thenReturn(Optional.of(produto));

        ProdutoResponseDTO response = service.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Mouse");
        assertThat(response.preco()).isEqualByComparingTo("59.50");
        assertThat(response.quantidadeEstoque()).isEqualTo(3);
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(produtoDAO.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado com id 99");
    }

    @Test
    void deveListarTodosOsProdutos() {
        when(produtoDAO.listarTodos()).thenReturn(List.of(
                new Produto(1L, "Teclado", null, new BigDecimal("199.90"), 10),
                new Produto(2L, "Mouse", null, new BigDecimal("59.50"), 0)));

        List<ProdutoResponseDTO> produtos = service.listarTodos();

        assertThat(produtos).extracting(ProdutoResponseDTO::nome).containsExactly("Teclado", "Mouse");
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaProdutos() {
        when(produtoDAO.listarTodos()).thenReturn(List.of());

        assertThat(service.listarTodos()).isEmpty();
    }

    private ProdutoRequestDTO novaRequest() {
        return new ProdutoRequestDTO("Teclado", "Teclado mecânico", new BigDecimal("199.90"), 10);
    }

}
