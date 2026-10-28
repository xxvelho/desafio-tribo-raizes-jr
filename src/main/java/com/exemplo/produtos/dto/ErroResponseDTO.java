package com.exemplo.produtos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

public record ErroResponseDTO(
        Instant timestamp,
        int status,
        String mensagem,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<CampoErro> erros
) {

    public ErroResponseDTO(int status, String mensagem) {
        this(status, mensagem, List.of());
    }

    public ErroResponseDTO(int status, String mensagem, List<CampoErro> erros) {
        this(Instant.now(), status, mensagem, erros);
    }

    public record CampoErro(String campo, String mensagem) {
    }

}
