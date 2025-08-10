package com.banco.banca.web.controller;

import com.banco.banca.domain.entity.Cliente;
import com.banco.banca.domain.service.ClienteService;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Si tienes @ControllerAdvice personalizado, lo añades aquí:
        // mockMvc = MockMvcBuilders.standaloneSetup(clienteController)
        //         .setControllerAdvice(new GlobalExceptionHandler())
        //         .build();
        mockMvc = MockMvcBuilders.standaloneSetup(clienteController).build();
    }

    @Test
    void postCliente_Valido_201() throws Exception {
        Cliente creado = Cliente.builder()
                .id(UUID.randomUUID())
                .tipoIdentificacion("CC")
                .numIdentificacion("123")
                .nombres("Juan")
                .apellidos("Pérez")
                .fechaNacimiento(LocalDate.now().minusYears(20))
                .build();

        when(clienteService.crear(any(Cliente.class))).thenReturn(creado);

        Map<String, Object> body = new HashMap<>();
        body.put("nombres", "Juan");
        body.put("apellidos", "Pérez");
        body.put("tipoIdentificacion", "CC");
        body.put("numIdentificacion", "123");
        body.put("fechaNacimiento", "2005-01-01");

        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void getClientes_200() throws Exception {
        when(clienteService.buscar(any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk());
    }
}


