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

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping
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
    public ClienteResponse obtener(@PathVariable(name = "id") UUID id){
        return ClienteResponse.fromEntity(clienteService.obtener(id));
    }

    @PostMapping
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
    public ResponseEntity<Void> eliminar(@PathVariable(name = "id") UUID id){
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

