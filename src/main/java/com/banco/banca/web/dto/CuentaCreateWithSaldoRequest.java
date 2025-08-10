package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CuentaCreateWithSaldoRequest {
    
    @NotNull(message = "clienteId es requerido")
    private UUID clienteId;
    
    @NotNull(message = "tipoCuenta es requerido")
    private TipoCuenta tipoCuenta;
    
    @NotNull(message = "saldoInicial es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El saldo inicial debe ser mayor a 0")
    private BigDecimal saldoInicial;
    
    @NotNull(message = "exentaGmf es requerido")
    private Boolean exentaGmf;
    
    private String usuarioPropietario;
}
