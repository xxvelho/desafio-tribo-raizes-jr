package com.exemplo.produtos.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoRequestDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        factory.close();
    }

    @Test
    void deveAceitarRequestValida() {
        ProdutoRequestDTO request = new ProdutoRequestDTO("Teclado", "Teclado mecânico", new BigDecimal("199.90"), 10);

        assertThat(camposInvalidos(request)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void deveRejeitarNomeNuloVazioOuEmBranco(String nome) {
        assertThat(camposInvalidos(comNome(nome))).containsExactly("nome");
    }

    @Test
    void deveAceitarNomeCom150Caracteres() {
        assertThat(camposInvalidos(comNome("x".repeat(150)))).isEmpty();
    }

    @Test
    void deveRejeitarNomeCom151Caracteres() {
        assertThat(camposInvalidos(comNome("x".repeat(151)))).containsExactly("nome");
    }

    @Test
    void deveAceitarDescricaoNula() {
        assertThat(camposInvalidos(comDescricao(null))).isEmpty();
    }

    @Test
    void deveAceitarDescricaoCom500Caracteres() {
        assertThat(camposInvalidos(comDescricao("x".repeat(500)))).isEmpty();
    }

    @Test
    void deveRejeitarDescricaoCom501Caracteres() {
        assertThat(camposInvalidos(comDescricao("x".repeat(501)))).containsExactly("descricao");
    }

    @Test
    void deveRejeitarPrecoNulo() {
        assertThat(camposInvalidos(comPreco(null))).containsExactly("preco");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-10"})
    void deveRejeitarPrecoZeroOuNegativo(String preco) {
        assertThat(camposInvalidos(comPreco(new BigDecimal(preco)))).containsExactly("preco");
    }

    @Test
    void deveRejeitarPrecoComMaisDeDuasCasasDecimais() {
        assertThat(camposInvalidos(comPreco(new BigDecimal("1.999")))).containsExactly("preco");
    }

    @Test
    void deveAceitarPrecoComDezDigitosInteiros() {
        assertThat(camposInvalidos(comPreco(new BigDecimal("9999999999.99")))).isEmpty();
    }

    @Test
    void deveRejeitarPrecoComOnzeDigitosInteiros() {
        assertThat(camposInvalidos(comPreco(new BigDecimal("10000000000.00")))).containsExactly("preco");
    }

    @Test
    void deveRejeitarEstoqueNulo() {
        assertThat(camposInvalidos(comEstoque(null))).containsExactly("quantidadeEstoque");
    }

    @Test
    void deveRejeitarEstoqueNegativo() {
        assertThat(camposInvalidos(comEstoque(-1))).containsExactly("quantidadeEstoque");
    }

    @Test
    void deveAceitarEstoqueZero() {
        assertThat(camposInvalidos(comEstoque(0))).isEmpty();
    }

    private Set<String> camposInvalidos(ProdutoRequestDTO request) {
        return validator.validate(request).stream()
                .map(violacao -> violacao.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private ProdutoRequestDTO comNome(String nome) {
        return new ProdutoRequestDTO(nome, "Teclado mecânico", new BigDecimal("199.90"), 10);
    }

    private ProdutoRequestDTO comDescricao(String descricao) {
        return new ProdutoRequestDTO("Teclado", descricao, new BigDecimal("199.90"), 10);
    }

    private ProdutoRequestDTO comPreco(BigDecimal preco) {
        return new ProdutoRequestDTO("Teclado", "Teclado mecânico", preco, 10);
    }

    private ProdutoRequestDTO comEstoque(Integer quantidadeEstoque) {
        return new ProdutoRequestDTO("Teclado", "Teclado mecânico", new BigDecimal("199.90"), quantidadeEstoque);
    }

}
