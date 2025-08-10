package com.banco.banca.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ConsignacionRequest {
    @NotNull
    private UUID cuentaDestinoId;
    @NotNull @Positive
    private BigDecimal monto;
    private String descripcion;
}

