package com.banco.banca.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "API Bancaria",
        version = "v1",
        description = "Sistema bancario REST API para gestionar clientes, cuentas y transacciones."
    ),
    servers = {
        @Server(url = "http://localhost:8000", description = "Local")
    }
)
@Configuration
public class OpenApiConfig {
}