package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.EstadoCuenta;
import com.banco.banca.domain.entity.TipoCuenta;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CuentaCreateRequest {
    @NotNull
    private UUID clienteId;
    @NotNull
    private TipoCuenta tipoCuenta;
    @NotNull(message = "exentaGmf es requerido")
    private Boolean exentaGmf;
    private String usuarioPropietario;
}

