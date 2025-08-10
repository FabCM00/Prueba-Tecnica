package com.banco.banca.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transacciones")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Transaccion {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransaccion tipo;

    @Column(nullable = false)
    private Instant fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_origen_id")
    private Cuenta cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id")
    private Cuenta cuentaDestino;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    private String descripcion;

    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTransaccion estado;

    @Column(name = "creado_por")
    private String creadoPor;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (fecha == null) fecha = Instant.now();
    }
}

