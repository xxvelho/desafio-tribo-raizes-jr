package com.exemplo.produtos.exception;

import com.exemplo.produtos.dto.ErroResponseDTO;
import com.exemplo.produtos.dto.ErroResponseDTO.CampoErro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarProdutoNaoEncontrado(ProdutoNaoEncontradoException ex) {
        ErroResponseDTO erro = new ErroResponseDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarErroInesperado(Exception ex) {
        log.error("Erro inesperado ao processar a requisição", ex);
        ErroResponseDTO erro = new ErroResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), mensagemPara(HttpStatus.INTERNAL_SERVER_ERROR));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<CampoErro> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new CampoErro(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        ErroResponseDTO erro = new ErroResponseDTO(status.value(), "Dados inválidos", erros);
        return ResponseEntity.status(status).body(erro);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErroResponseDTO erro = new ErroResponseDTO(status.value(), "Corpo da requisição inválido ou malformado");
        return ResponseEntity.status(status).body(erro);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErroResponseDTO erro = new ErroResponseDTO(status.value(), "O valor informado na URL tem formato inválido");
        return ResponseEntity.status(status).body(erro);
    }

    // Demais exceções do Spring MVC (405, 415, rota inexistente...): mantém o status e os headers originais
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErroResponseDTO erro = new ErroResponseDTO(status.value(), mensagemPara(status));
        return ResponseEntity.status(status).headers(headers).body(erro);
    }

    private String mensagemPara(HttpStatusCode status) {
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
