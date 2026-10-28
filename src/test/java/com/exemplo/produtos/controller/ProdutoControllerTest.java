package com.exemplo.produtos.controller;

import com.exemplo.produtos.dto.ProdutoRequestDTO;
import com.exemplo.produtos.dto.ProdutoResponseDTO;
import com.exemplo.produtos.exception.ProdutoNaoEncontradoException;
import com.exemplo.produtos.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
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
    void deveMontarLocationSemQueryString() throws Exception {
        when(produtoService.criar(any(ProdutoRequestDTO.class))).thenReturn(teclado());

        mockMvc.perform(post("/produtos?origem=teste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/produtos/1")));
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
    void deveRetornar400QuandoPrecoTemExpoenteGigante() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Teclado", "preco": 1e2147483647, "quantidadeEstoque": 10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", contains("preco")));

        verifyNoInteractions(produtoService);
    }

    @Test
    void deveOrdenarErrosDeValidacaoPorCampo() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "preco": -10, "quantidadeEstoque": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", contains("nome", "preco", "quantidadeEstoque")));
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
    void deveRetornar400QuandoIdNaUrlForInvalido() throws Exception {
        mockMvc.perform(get("/produtos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("O valor informado na URL tem formato inválido"));

        verifyNoInteractions(produtoService);
    }

    @Test
    void deveRetornar415QuandoContentTypeNaoForJson() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("teste"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.mensagem").value("Tipo de conteúdo não suportado"));
    }

    @Test
    void deveRetornar405QuandoMetodoNaoForSuportado() throws Exception {
        mockMvc.perform(patch("/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void deveRetornar404QuandoARotaNaoExistir() throws Exception {
        mockMvc.perform(get("/rota-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Recurso não encontrado"));
    }

    @Test
    void deveRetornar404EmJsonMesmoQuandoOClienteNaoAceitaJson() throws Exception {
        when(produtoService.buscarPorId(99L)).thenThrow(new ProdutoNaoEncontradoException(99L));

        mockMvc.perform(get("/produtos/99").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensagem").value("Produto não encontrado com id 99"));
    }

    @Test
    void deveRetornar406EmJsonQuandoOClienteSoAceitaOutroFormato() throws Exception {
        mockMvc.perform(get("/produtos").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(406))
                .andExpect(jsonPath("$.mensagem").value("Formato de resposta não suportado"));
    }

    @Test
    void deveRegistrarNoLogOsErros5xxDoSpringMvc(CapturedOutput output) throws Exception {
        when(produtoService.listarTodos()).thenThrow(new HttpMessageNotWritableException("falha ao escrever"));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro interno do servidor"));

        assertThat(output.getAll()).contains("falha ao escrever");
    }

    @Test
    void deveRetornar500SemExporDetalhesDoErro() throws Exception {
        when(produtoService.listarTodos()).thenThrow(new IllegalStateException("falha interna secreta"));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensagem").value("Erro interno do servidor"))
                .andExpect(content().string(not(containsString("secreta"))));
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
