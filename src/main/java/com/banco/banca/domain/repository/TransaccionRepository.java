package com.banco.banca.domain.repository;

import com.banco.banca.domain.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TransaccionRepository extends JpaRepository<Transaccion, UUID>, JpaSpecificationExecutor<Transaccion> {
}

