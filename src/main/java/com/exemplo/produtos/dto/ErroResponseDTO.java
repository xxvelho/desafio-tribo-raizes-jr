package com.exemplo.produtos.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponseDTO(
        LocalDateTime timestamp,
        int status,
        String mensagem,
        List<CampoErro> erros
) {

    public ErroResponseDTO(int status, String mensagem) {
        this(status, mensagem, List.of());
    }

    public ErroResponseDTO(int status, String mensagem, List<CampoErro> erros) {
        this(LocalDateTime.now(), status, mensagem, erros);
    }

    public record CampoErro(String campo, String mensagem) {
    }

}
