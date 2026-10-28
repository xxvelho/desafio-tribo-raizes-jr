package com.exemplo.produtos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI produtosOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("API de Produtos")
                .description("API REST para criação, atualização, remoção e consulta de produtos")
                .version("1.0.0"));
    }

}
