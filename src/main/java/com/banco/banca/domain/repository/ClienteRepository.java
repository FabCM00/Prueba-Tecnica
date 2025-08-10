package com.banco.banca.domain.repository;

import com.banco.banca.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByNumIdentificacion(String numIdentificacion);
    boolean existsByNumIdentificacion(String numIdentificacion);
    
    // Validación de unicidad compuesta
    boolean existsByTipoIdentificacionAndNumIdentificacion(String tipoIdentificacion, String numIdentificacion);
    
    // Verificar si cliente tiene cuentas asociadas
    @Query("SELECT COUNT(c) > 0 FROM Cuenta c WHERE c.cliente.id = :clienteId")
    boolean hasCuentasAsociadas(@Param("clienteId") UUID clienteId);

    // Búsqueda con filtros opcionales (nativa) usando ILIKE sobre VARCHAR
    @Query(value = """
            SELECT c.*
            FROM clientes c
            WHERE (:tipoIdentificacion IS NULL OR c.tipo_identificacion = :tipoIdentificacion)
              AND (:numIdentificacion IS NULL OR c.num_identificacion = :numIdentificacion)
              AND (:nombre IS NULL OR c.nombres ILIKE CONCAT('%', :nombre, '%'))
              AND (:apellido IS NULL OR c.apellidos ILIKE CONCAT('%', :apellido, '%'))
              AND (:email IS NULL OR c.email ILIKE CONCAT('%', :email, '%'))
            ORDER BY c.fecha_creacion DESC
            """, nativeQuery = true)
    List<Cliente> search(
            @Param("tipoIdentificacion") String tipoIdentificacion,
            @Param("numIdentificacion") String numIdentificacion,
            @Param("nombre") String nombre,
            @Param("apellido") String apellido,
            @Param("email") String email
    );
}

