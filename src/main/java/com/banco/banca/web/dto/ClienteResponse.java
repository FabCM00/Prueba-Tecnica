package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.Cliente;
import lombok.Data;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Data
public class ClienteResponse {
    private UUID id;
    private String tipoIdentificacion;
    private String numIdentificacion;
    private String nombres;
    private String apellidos;
    private String email;
    private LocalDate fechaNacimiento;
    private Instant fechaCreacion;
    private Instant fechaModificacion;
    private Long version;

    public static ClienteResponse fromEntity(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setId(cliente.getId());
        response.setTipoIdentificacion(cliente.getTipoIdentificacion());
        response.setNumIdentificacion(cliente.getNumIdentificacion());
        response.setNombres(cliente.getNombres());
        response.setApellidos(cliente.getApellidos());
        response.setEmail(cliente.getEmail());
        response.setFechaNacimiento(cliente.getFechaNacimiento());
        response.setFechaCreacion(cliente.getFechaCreacion());
        response.setFechaModificacion(cliente.getFechaModificacion());
        response.setVersion(cliente.getVersion());
        return response;
    }
}
