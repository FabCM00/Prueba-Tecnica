package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.EstadoCuenta;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CuentaUpdateEstadoRequest {
    @NotNull
    private EstadoCuenta estado;
}

