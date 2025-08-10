package com.banco.banca.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movimientos")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Movimiento {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "transaccion_id", nullable = false)
    private Transaccion transaccion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_mov", nullable = false)
    private TipoMovimiento tipoMov;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @Column(name = "saldo_antes", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoAntes;

    @Column(name = "saldo_despues", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoDespues;

    @Column(nullable = false)
    private Instant fecha;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (fecha == null) fecha = Instant.now();
    }
}

