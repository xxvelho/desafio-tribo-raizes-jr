package com.exemplo.produtos.exception;

import com.exemplo.produtos.dto.ErroResponseDTO;
import com.exemplo.produtos.dto.ErroResponseDTO.CampoErro;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarProdutoNaoEncontrado(ProdutoNaoEncontradoException ex) {
        ErroResponseDTO erro = new ErroResponseDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex) {
        List<CampoErro> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new CampoErro(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        ErroResponseDTO erro = new ErroResponseDTO(HttpStatus.BAD_REQUEST.value(), "Dados inválidos", erros);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
        ErroResponseDTO erro = new ErroResponseDTO(
                HttpStatus.BAD_REQUEST.value(), "Corpo da requisição inválido ou malformado");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

}
