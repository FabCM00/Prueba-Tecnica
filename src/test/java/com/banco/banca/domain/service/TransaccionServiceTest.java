package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.common.exception.NotFoundException;
import com.banco.banca.domain.entity.*;
import com.banco.banca.domain.repository.CuentaRepository;
import com.banco.banca.domain.repository.MovimientoRepository;
import com.banco.banca.domain.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private TransaccionRepository transaccionRepository;
    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private TransaccionService transaccionService;

    private UUID cuentaAId;
    private Cuenta cuentaA;

    @BeforeEach
    void setup() {
        cuentaAId = UUID.randomUUID();
        cuentaA = Cuenta.builder()
                .id(cuentaAId)
                .tipoCuenta(TipoCuenta.AHORROS)
                .estado(EstadoCuenta.ACTIVA)
                .saldo(new BigDecimal("100"))
                .build();
    }

    @Test
    void retirar_Ahorros_FondosInsuficientes_LanzaConflicto() {
        when(cuentaRepository.findWithLockingById(cuentaAId)).thenReturn(Optional.of(cuentaA));
        BusinessException ex = assertThrows(BusinessException.class, () ->
                transaccionService.retirar(cuentaAId, new BigDecimal("200"), "test", "user")
        );
        assertTrue(ex.getMessage().contains("Fondos insuficientes"));
    }

    @Test
    void transferir_MismaCuenta_LanzaBadRequest() {
        UUID id = UUID.randomUUID();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                transaccionService.transferir(id, id, new BigDecimal("10"), "test", "user")
        );
        assertTrue(ex.getMessage().contains("no pueden ser iguales"));
    }

    @Test
    void consignar_Existosa_DebeCrearTxYMovimiento() {
        when(cuentaRepository.findWithLockingById(cuentaAId)).thenReturn(Optional.of(cuentaA));
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(inv -> {
            Transaccion tx = inv.getArgument(0);
            tx.setId(UUID.randomUUID());
            return tx;
        });
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        var resp = transaccionService.consignar(cuentaAId, new BigDecimal("50"), "consignacion", "user");
        assertNotNull(resp.getId());
        assertEquals(TipoTransaccion.CONSIGNACION, resp.getTipo());
        assertEquals(new BigDecimal("50"), resp.getMonto());
    }
} 