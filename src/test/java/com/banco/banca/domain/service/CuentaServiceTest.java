package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.common.exception.NotFoundException;
import com.banco.banca.domain.entity.*;
import com.banco.banca.domain.repository.ClienteRepository;
import com.banco.banca.domain.repository.CuentaRepository;
import com.banco.banca.domain.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private CuentaService cuentaService;

    private UUID clienteId;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        cliente = Cliente.builder().id(clienteId).nombres("Juan").apellidos("Pérez").tipoIdentificacion("CC").numIdentificacion("1").build();
    }

    @Test
    void crear_CuentaAhorros_Exito_NumeroConPrefijo53() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.existsByNumeroCuenta(anyString())).thenReturn(false);
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(inv -> inv.getArgument(0));

        Cuenta creada = cuentaService.crear(clienteId, TipoCuenta.AHORROS, false, "user");

        assertNotNull(creada.getNumeroCuenta());
        assertTrue(creada.getNumeroCuenta().startsWith("53"));
        assertEquals(10, creada.getNumeroCuenta().length());
        assertEquals(EstadoCuenta.ACTIVA, creada.getEstado());
        assertEquals(BigDecimal.ZERO, creada.getSaldo());
    }

    @Test
    void crearConSaldo_SaldoNegativo_LanzaBAD_REQUEST() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                cuentaService.crearConSaldo(clienteId, TipoCuenta.AHORROS, new BigDecimal("-1"), false, "user")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("no puede ser negativo"));
    }

    @Test
    void crear_ClienteNoExiste_LanzaNotFound() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cuentaService.crear(clienteId, TipoCuenta.AHORROS, false, null));
    }

    @Test
    void actualizarEstado_CancelarConSaldoNoCero_Conflicto() {
        UUID cuentaId = UUID.randomUUID();
        Cuenta cuenta = Cuenta.builder()
                .id(cuentaId)
                .cliente(cliente)
                .tipoCuenta(TipoCuenta.AHORROS)
                .numeroCuenta("5300000000")
                .estado(EstadoCuenta.ACTIVA)
                .saldo(new BigDecimal("100"))
                .build();
        when(cuentaRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));

        BusinessException ex = assertThrows(BusinessException.class, () -> cuentaService.actualizarEstado(cuentaId, EstadoCuenta.CANCELADA));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void actualizarEstado_InactivarConSaldoNegativo_Conflicto() {
        UUID cuentaId = UUID.randomUUID();
        Cuenta cuenta = Cuenta.builder()
                .id(cuentaId)
                .cliente(cliente)
                .tipoCuenta(TipoCuenta.CORRIENTE)
                .numeroCuenta("3300000000")
                .estado(EstadoCuenta.ACTIVA)
                .saldo(new BigDecimal("-10"))
                .build();
        when(cuentaRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));

        BusinessException ex = assertThrows(BusinessException.class, () -> cuentaService.actualizarEstado(cuentaId, EstadoCuenta.INACTIVA));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }
} 