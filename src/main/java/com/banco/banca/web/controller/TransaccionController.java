package com.banco.banca.web.controller;

import com.banco.banca.domain.service.TransaccionService;
import com.banco.banca.web.dto.ConsignacionRequest;
import com.banco.banca.web.dto.RetiroRequest;
import com.banco.banca.web.dto.TransferenciaRequest;
import com.banco.banca.web.dto.TransaccionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Operaciones de consignación, retiro y transferencia")
public class TransaccionController {
    private final TransaccionService transaccionService;

    private String currentUser(String header){
        return header != null && !header.isBlank() ? header : "system";
    }

    @PostMapping("/consignacion")
    @Operation(summary = "Consignar a una cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transacción creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Reglas de negocio impiden la operación")
    })
    public ResponseEntity<TransaccionResponse> consignar(
            @Parameter(in = ParameterIn.HEADER, name = "X-User", description = "Usuario que ejecuta la operación", required = false)
            @RequestHeader(value="X-User", required=false) String user,
            @Valid @RequestBody ConsignacionRequest req){
        TransaccionResponse tx = transaccionService.consignar(req.getCuentaDestinoId(), req.getMonto(), req.getDescripcion(), currentUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PostMapping("/retiro")
    @Operation(summary = "Retirar de una cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transacción creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Reglas de negocio impiden la operación")
    })
    public ResponseEntity<TransaccionResponse> retirar(
            @Parameter(in = ParameterIn.HEADER, name = "X-User", description = "Usuario que ejecuta la operación", required = false)
            @RequestHeader(value="X-User", required=false) String user,
            @Valid @RequestBody RetiroRequest req){
        TransaccionResponse tx = transaccionService.retirar(req.getCuentaOrigenId(), req.getMonto(), req.getDescripcion(), currentUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PostMapping("/transferencia")
    @Operation(summary = "Transferir entre cuentas")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transacción creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Reglas de negocio impiden la operación")
    })
    public ResponseEntity<TransaccionResponse> transferir(
            @Parameter(in = ParameterIn.HEADER, name = "X-User", description = "Usuario que ejecuta la operación", required = false)
            @RequestHeader(value="X-User", required=false) String user,
            @Valid @RequestBody TransferenciaRequest req){
        TransaccionResponse tx = transaccionService.transferir(req.getCuentaOrigenId(), req.getCuentaDestinoId(), req.getMonto(), req.getDescripcion(), currentUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @GetMapping
    @Operation(summary = "Listar transacciones", description = "Filtra por cuenta y rango de fechas en ISO-8601")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de transacciones")
    })
    public List<TransaccionResponse> listar(
            @RequestParam(name = "cuenta", required = false) UUID cuenta,
            @RequestParam(name = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fechaHasta
    ){
        return transaccionService.buscar(cuenta, fechaDesde, fechaHasta);
    }
}

