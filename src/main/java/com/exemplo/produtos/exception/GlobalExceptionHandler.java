package com.exemplo.produtos.exception;

import com.exemplo.produtos.dto.ErroResponseDTO;
import com.exemplo.produtos.dto.ErroResponseDTO.CampoErro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<Object> tratarProdutoNaoEncontrado(ProdutoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> tratarErroInesperado(Exception ex) {
        log.error("Erro inesperado ao processar a requisição", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, mensagemPara(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<CampoErro> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new CampoErro(fieldError.getField(), fieldError.getDefaultMessage()))
                .sorted(Comparator.comparing(CampoErro::campo).thenComparing(CampoErro::mensagem))
                .toList();
        return resposta(status, "Dados inválidos", erros, headers);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return resposta(status, "Corpo da requisição inválido ou malformado");
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return resposta(status, "O valor informado na URL tem formato inválido");
    }

    // Demais exceções do Spring MVC (405, 415, rota inexistente...): mantém o status e os headers originais
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("Erro do servidor ao processar a requisição", ex);
        }
        return resposta(status, mensagemPara(status), List.of(), headers);
    }

    private static ResponseEntity<Object> resposta(HttpStatusCode status, String mensagem) {
        return resposta(status, mensagem, List.of(), HttpHeaders.EMPTY);
    }

    // O Content-Type fixo evita falha de negociação quando o cliente não aceita JSON (Accept: application/xml, por exemplo)
    private static ResponseEntity<Object> resposta(HttpStatusCode status, String mensagem,
            List<CampoErro> erros, HttpHeaders headers) {
        ErroResponseDTO erro = new ErroResponseDTO(status.value(), mensagem, erros);
        return ResponseEntity.status(status)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(erro);
    }

    private static String mensagemPara(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisição inválida";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método HTTP não suportado para este recurso";
            case 406 -> "Formato de resposta não suportado";
            case 415 -> "Tipo de conteúdo não suportado";
            case 500 -> "Erro interno do servidor";
            default -> "Não foi possível processar a requisição";
        };
    }

}
