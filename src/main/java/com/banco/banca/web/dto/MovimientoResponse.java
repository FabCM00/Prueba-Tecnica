package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.Movimiento;
import com.banco.banca.domain.entity.TipoMovimiento;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class MovimientoResponse {
    private UUID id;
    private UUID cuentaId;
    private TipoMovimiento tipoMov;
    private BigDecimal monto;
    private BigDecimal saldoAntes;
    private BigDecimal saldoDespues;
    private Instant fecha;

    public static MovimientoResponse fromEntity(Movimiento movimiento) {
        MovimientoResponse r = new MovimientoResponse();
        r.setId(movimiento.getId());
        r.setCuentaId(movimiento.getCuenta() != null ? movimiento.getCuenta().getId() : null);
        r.setTipoMov(movimiento.getTipoMov());
        r.setMonto(movimiento.getMonto());
        r.setSaldoAntes(movimiento.getSaldoAntes());
        r.setSaldoDespues(movimiento.getSaldoDespues());
        r.setFecha(movimiento.getFecha());
        return r;
    }
} 