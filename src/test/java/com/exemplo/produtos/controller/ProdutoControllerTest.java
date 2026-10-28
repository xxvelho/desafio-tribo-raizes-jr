package com.exemplo.produtos.controller;

import com.exemplo.produtos.dto.ProdutoRequestDTO;
import com.exemplo.produtos.dto.ProdutoResponseDTO;
import com.exemplo.produtos.exception.ProdutoNaoEncontradoException;
import com.exemplo.produtos.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    private static final String JSON_VALIDO = """
            {"nome": "Teclado", "descricao": "Teclado mecânico", "preco": 199.90, "quantidadeEstoque": 10}
            """;

    private static final String JSON_INVALIDO = """
            {"nome": " ", "preco": -5, "quantidadeEstoque": 10}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService produtoService;

    @Test
    void deveCriarProdutoERetornar201ComLocation() throws Exception {
        when(produtoService.criar(any(ProdutoRequestDTO.class))).thenReturn(teclado());

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/produtos/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Teclado"))
                .andExpect(jsonPath("$.preco").value(199.90))
                .andExpect(jsonPath("$.quantidadeEstoque").value(10));
    }

    @Test
    void deveRetornar400AoCriarProdutoInvalido() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("nome", "preco")));

        verifyNoInteractions(produtoService);
    }

    @Test
    void deveRetornar400AoReceberJsonMalformado() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nome: "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(produtoService);
    }

    @Test
    void deveRetornar400QuandoEstoqueForDecimal() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Teclado", "preco": 199.90, "quantidadeEstoque": 10.7}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido ou malformado"));

        verifyNoInteractions(produtoService);
    }

    @Test
    void deveListarProdutos() throws Exception {
        when(produtoService.listarTodos()).thenReturn(List.of(teclado()));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Teclado"));
    }

    @Test
    void deveBuscarProdutoPorId() throws Exception {
        when(produtoService.buscarPorId(1L)).thenReturn(teclado());

        mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Teclado"));
    }

    @Test
    void deveRetornar404AoBuscarIdInexistente() throws Exception {
        when(produtoService.buscarPorId(99L)).thenThrow(new ProdutoNaoEncontradoException(99L));

        mockMvc.perform(get("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp", endsWith("Z")))
                .andExpect(jsonPath("$.erros").doesNotExist())
                .andExpect(jsonPath("$.mensagem").value("Produto não encontrado com id 99"));
    }

    @Test
    void deveAtualizarProduto() throws Exception {
        when(produtoService.atualizar(any(Long.class), any(ProdutoRequestDTO.class))).thenReturn(teclado());

        mockMvc.perform(put("/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Teclado"));
    }

    @Test
    void deveRetornar400AoAtualizarComDadosInvalidos() throws Exception {
        mockMvc.perform(put("/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"));

        verifyNoInteractions(produtoService);
    }

    @Test
    void deveRetornar404AoAtualizarIdInexistente() throws Exception {
        when(produtoService.atualizar(any(Long.class), any(ProdutoRequestDTO.class)))
                .thenThrow(new ProdutoNaoEncontradoException(99L));

        mockMvc.perform(put("/produtos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Produto não encontrado com id 99"));
    }

    @Test
    void deveRemoverProdutoERetornar204() throws Exception {
        mockMvc.perform(delete("/produtos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar404AoRemoverIdInexistente() throws Exception {
        doThrow(new ProdutoNaoEncontradoException(99L)).when(produtoService).remover(99L);

        mockMvc.perform(delete("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Produto não encontrado com id 99"));
    }

    private ProdutoResponseDTO teclado() {
        return new ProdutoResponseDTO(1L, "Teclado", "Teclado mecânico", new BigDecimal("199.90"), 10);
    }

}
