package com.banco.banca.domain.repository;

import com.banco.banca.domain.entity.Cuenta;
import com.banco.banca.domain.entity.EstadoCuenta;
import com.banco.banca.domain.entity.TipoCuenta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CuentaRepository extends JpaRepository<Cuenta, UUID> {
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);
    boolean existsByNumeroCuenta(String numeroCuenta);
    boolean existsByCliente_Id(UUID clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Cuenta> findWithLockingById(UUID id);

    // Búsqueda con filtros opcionales
    @Query("""
            SELECT c FROM Cuenta c
            WHERE (:clienteId IS NULL OR c.cliente.id = :clienteId)
              AND (:tipoCuenta IS NULL OR c.tipoCuenta = :tipoCuenta)
              AND (:estado IS NULL OR c.estado = :estado)
              AND (:numeroCuenta IS NULL OR c.numeroCuenta = :numeroCuenta)
            ORDER BY c.fechaCreacion DESC
            """)
    List<Cuenta> search(
            @Param("clienteId") UUID clienteId,
            @Param("tipoCuenta") TipoCuenta tipoCuenta,
            @Param("estado") EstadoCuenta estado,
            @Param("numeroCuenta") String numeroCuenta
    );
}

