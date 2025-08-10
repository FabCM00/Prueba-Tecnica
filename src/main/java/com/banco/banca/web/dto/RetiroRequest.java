package com.banco.banca.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RetiroRequest {
    @NotNull
    private UUID cuentaOrigenId;
    @NotNull @Positive
    private BigDecimal monto;
    private String descripcion;
}

