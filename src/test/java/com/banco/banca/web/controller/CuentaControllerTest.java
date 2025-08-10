package com.banco.banca.web.controller;

import com.banco.banca.domain.entity.Cuenta;
import com.banco.banca.domain.entity.EstadoCuenta;
import com.banco.banca.domain.entity.TipoCuenta;
import com.banco.banca.domain.service.CuentaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CuentaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CuentaService cuentaService;

    @InjectMocks
    private CuentaController cuentaController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Si tienes un @ControllerAdvice global, descomenta y añade la clase:
        // mockMvc = MockMvcBuilders.standaloneSetup(cuentaController)
        //         .setControllerAdvice(new GlobalExceptionHandler())
        //         .build();
        mockMvc = MockMvcBuilders.standaloneSetup(cuentaController).build();
    }

    @Test
    void postCuenta_Crea_201() throws Exception {
        Cuenta cuenta = Cuenta.builder()
                .id(UUID.randomUUID())
                .tipoCuenta(TipoCuenta.AHORROS)
                .numeroCuenta("5300000000")
                .estado(EstadoCuenta.ACTIVA)
                .saldo(BigDecimal.ZERO)
                .build();

        when(cuentaService.crear(any(), any(), any(), any())).thenReturn(cuenta);

        Map<String, Object> body = new HashMap<>();
        body.put("clienteId", UUID.randomUUID().toString());
        body.put("tipoCuenta", "AHORROS");
        body.put("exentaGmf", false);

        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(
                        post("/api/cuentas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void putCuenta_ActualizaEstado_200() throws Exception {
        Cuenta cuenta = Cuenta.builder()
                .id(UUID.randomUUID())
                .tipoCuenta(TipoCuenta.AHORROS)
                .numeroCuenta("5300000000")
                .estado(EstadoCuenta.INACTIVA)
                .saldo(BigDecimal.ZERO)
                .build();

        when(cuentaService.actualizarEstado(any(UUID.class), any())).thenReturn(cuenta);

        Map<String, Object> body = new HashMap<>();
        body.put("estado", "INACTIVA");
        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(
                        put("/api/cuentas/" + cuenta.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }
}
