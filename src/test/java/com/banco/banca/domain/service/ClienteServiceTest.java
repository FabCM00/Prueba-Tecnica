package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.domain.entity.Cliente;
import com.banco.banca.domain.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteValido;

    @BeforeEach
    void setUp() {
        clienteValido = Cliente.builder()
                .id(UUID.randomUUID())
                .tipoIdentificacion("CC")
                .numIdentificacion("12345678")
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan.perez@email.com")
                .fechaNacimiento(LocalDate.now().minusYears(25))
                .build();
    }

    @Test
    void crearCliente_ConDatosValidos_DebeCrearExitosamente() {
        // Arrange
        when(clienteRepository.existsByTipoIdentificacionAndNumIdentificacion(any(), any()))
                .thenReturn(false);
        when(clienteRepository.save(any())).thenReturn(clienteValido);

        // Act
        Cliente resultado = clienteService.crear(clienteValido);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository).save(clienteValido);
    }

    @Test
    void crearCliente_MenorDeEdad_DebeLanzarExcepcion() {
        // Arrange
        clienteValido.setFechaNacimiento(LocalDate.now().minusYears(17));

        // Act & Assert
        BusinessException excepcion = assertThrows(BusinessException.class, 
                () -> clienteService.crear(clienteValido));
        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        assertEquals("No se puede crear cliente menor de edad", excepcion.getMessage());
    }

    @Test
    void crearCliente_IdentificacionDuplicada_DebeLanzarExcepcion() {
        // Arrange
        when(clienteRepository.existsByTipoIdentificacionAndNumIdentificacion(any(), any()))
                .thenReturn(true);

        // Act & Assert
        BusinessException excepcion = assertThrows(BusinessException.class, 
                () -> clienteService.crear(clienteValido));
        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
        assertEquals("Ya existe un cliente con el mismo tipo y número de identificación", excepcion.getMessage());
    }

    @Test
    void crearCliente_NombresCortos_DebeLanzarExcepcion() {
        // Arrange
        clienteValido.setNombres("A");

        // Act & Assert
        BusinessException excepcion = assertThrows(BusinessException.class, 
                () -> clienteService.crear(clienteValido));
        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        assertEquals("Nombres deben tener al menos 2 caracteres", excepcion.getMessage());
    }

    @Test
    void crearCliente_ApellidosCortos_DebeLanzarExcepcion() {
        // Arrange
        clienteValido.setApellidos("B");

        // Act & Assert
        BusinessException excepcion = assertThrows(BusinessException.class, 
                () -> clienteService.crear(clienteValido));
        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        assertEquals("Apellidos deben tener al menos 2 caracteres", excepcion.getMessage());
    }

    @Test
    void crearCliente_EmailInvalido_DebeLanzarExcepcion() {
        // Arrange
        clienteValido.setEmail("email-invalido");

        // Act & Assert
        BusinessException excepcion = assertThrows(BusinessException.class, 
                () -> clienteService.crear(clienteValido));
        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        assertEquals("Formato de email inválido", excepcion.getMessage());
    }

    @Test
    void crearCliente_EmailNull_DebeCrearExitosamente() {
        // Arrange
        clienteValido.setEmail(null);
        when(clienteRepository.existsByTipoIdentificacionAndNumIdentificacion(any(), any()))
                .thenReturn(false);
        when(clienteRepository.save(any())).thenReturn(clienteValido);

        // Act
        Cliente resultado = clienteService.crear(clienteValido);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository).save(clienteValido);
    }

    @Test
    void crearCliente_EmailVacio_DebeCrearExitosamente() {
        // Arrange
        clienteValido.setEmail("");
        when(clienteRepository.existsByTipoIdentificacionAndNumIdentificacion(any(), any()))
                .thenReturn(false);
        when(clienteRepository.save(any())).thenReturn(clienteValido);

        // Act
        Cliente resultado = clienteService.crear(clienteValido);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository).save(clienteValido);
    }
}

