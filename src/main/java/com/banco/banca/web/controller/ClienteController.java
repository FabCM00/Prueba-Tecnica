package com.banco.banca.web.controller;

import com.banco.banca.domain.entity.Cliente;
import com.banco.banca.domain.service.ClienteService;
import com.banco.banca.web.dto.ClienteRequest;
import com.banco.banca.web.dto.ClienteResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes")
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Filtra por tipo/número de identificación, nombre, apellido y email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de clientes")
    })
    public List<ClienteResponse> listar(
            @RequestParam(name = "tipoIdentificacion", required = false) String tipoIdentificacion,
            @RequestParam(name = "numIdentificacion", required = false) String numIdentificacion,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "apellido", required = false) String apellido,
            @RequestParam(name = "email", required = false) String email
    ){
        return clienteService.buscar(tipoIdentificacion, numIdentificacion, nombre, apellido, email).stream()
                .map(ClienteResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ClienteResponse obtener(@PathVariable(name = "id") UUID id){
        return ClienteResponse.fromEntity(clienteService.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "409", description = "Conflicto de datos")
    })
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest req){
        Cliente c = Cliente.builder()
                .tipoIdentificacion(req.getTipoIdentificacion())
                .numIdentificacion(req.getNumIdentificacion())
                .nombres(req.getNombres())
                .apellidos(req.getApellidos())
                .email(req.getEmail())
                .fechaNacimiento(req.getFechaNacimiento())
                .build();
        Cliente creado = clienteService.crear(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteResponse.fromEntity(creado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ClienteResponse actualizar(@PathVariable(name = "id") UUID id, @Valid @RequestBody ClienteRequest req){
        Cliente c = Cliente.builder()
                .tipoIdentificacion(req.getTipoIdentificacion())
                .numIdentificacion(req.getNumIdentificacion())
                .nombres(req.getNombres())
                .apellidos(req.getApellidos())
                .email(req.getEmail())
                .fechaNacimiento(req.getFechaNacimiento())
                .build();
        return ClienteResponse.fromEntity(clienteService.actualizar(id, c));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente eliminado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable(name = "id") UUID id){
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

