package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.Cuenta;
import com.banco.banca.domain.entity.EstadoCuenta;
import com.banco.banca.domain.entity.TipoCuenta;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class CuentaResponse {
    private UUID id;
    private UUID clienteId;
    private TipoCuenta tipoCuenta;
    private String numeroCuenta;
    private EstadoCuenta estado;
    private BigDecimal saldo;
    private boolean exentaGmf;
    private String usuarioPropietario;
    private Instant fechaCreacion;
    private Instant fechaModificacion;
    private Long version;

    public static CuentaResponse fromEntity(Cuenta cuenta) {
        CuentaResponse response = new CuentaResponse();
        response.setId(cuenta.getId());
        response.setClienteId(cuenta.getCliente() != null ? cuenta.getCliente().getId() : null);
        response.setTipoCuenta(cuenta.getTipoCuenta());
        response.setNumeroCuenta(cuenta.getNumeroCuenta());
        response.setEstado(cuenta.getEstado());
        response.setSaldo(cuenta.getSaldo());
        response.setExentaGmf(cuenta.isExentaGmf());
        response.setUsuarioPropietario(cuenta.getUsuarioPropietario());
        response.setFechaCreacion(cuenta.getFechaCreacion());
        response.setFechaModificacion(cuenta.getFechaModificacion());
        response.setVersion(cuenta.getVersion());
        return response;
    }
}
