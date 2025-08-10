package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.Transaccion;
import com.banco.banca.domain.entity.EstadoTransaccion;
import com.banco.banca.domain.entity.TipoTransaccion;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class TransaccionResponse {
    private UUID id;
    private TipoTransaccion tipo;
    private Instant fecha;
    private UUID cuentaOrigenId;
    private UUID cuentaDestinoId;
    private BigDecimal monto;
    private String descripcion;
    private String referencia;
    private EstadoTransaccion estado;
    private String creadoPor;

    public static TransaccionResponse fromEntity(Transaccion transaccion) {
        TransaccionResponse response = new TransaccionResponse();
        response.setId(transaccion.getId());
        response.setTipo(transaccion.getTipo());
        response.setFecha(transaccion.getFecha());
        
        if (transaccion.getCuentaOrigen() != null) {
            response.setCuentaOrigenId(transaccion.getCuentaOrigen().getId());
        }
        
        if (transaccion.getCuentaDestino() != null) {
            response.setCuentaDestinoId(transaccion.getCuentaDestino().getId());
        }
        
        response.setMonto(transaccion.getMonto());
        response.setDescripcion(transaccion.getDescripcion());
        response.setReferencia(transaccion.getReferencia());
        response.setEstado(transaccion.getEstado());
        response.setCreadoPor(transaccion.getCreadoPor());
        return response;
    }
}
