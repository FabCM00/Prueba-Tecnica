package com.banco.banca.web.controller;

import com.banco.banca.web.dto.TransaccionResponse;
import com.banco.banca.domain.entity.TipoTransaccion;
import com.banco.banca.domain.service.TransaccionService;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransaccionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransaccionService transaccionService;

    @InjectMocks
    private TransaccionController transaccionController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transaccionController).build();
    }

    @Test
    void postConsignacion_201() throws Exception {
        TransaccionResponse resp = new TransaccionResponse();
        resp.setId(UUID.randomUUID());
        resp.setTipo(TipoTransaccion.CONSIGNACION);

        when(transaccionService.consignar(any(), any(), any(), any())).thenReturn(resp);

        Map<String, Object> body = new HashMap<>();
        body.put("cuentaDestinoId", UUID.randomUUID().toString());
        body.put("monto", 1000.00);
        body.put("descripcion", "prueba");

        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(
                        post("/api/transacciones/consignacion")
                                .header("X-User", "tester") // si tu controlador lee este header, queda aquí
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void getTransaccionesConFiltros_200() throws Exception {
        when(transaccionService.buscar(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/transacciones")
                                .param("cuenta", UUID.randomUUID().toString())
                                .param("fechaDesde", Instant.now().toString())
                )
                .andExpect(status().isOk());
    }
}