package com.banco.banca.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ClienteRequest {
    @NotBlank
    private String tipoIdentificacion;
    @NotBlank
    private String numIdentificacion;
    @NotBlank @Size(min = 2)
    private String nombres;
    @NotBlank @Size(min = 2)
    private String apellidos;
    @Email
    private String email;
    @NotNull
    private LocalDate fechaNacimiento;
}

