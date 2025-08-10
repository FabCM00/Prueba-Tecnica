package com.banco.banca.domain.repository;

import com.banco.banca.domain.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovimientoRepository extends JpaRepository<Movimiento, UUID> {
    List<Movimiento> findTop10ByCuenta_IdOrderByFechaDesc(UUID cuentaId);
}

