package com.banco.banca.web.controller;

import com.banco.banca.domain.entity.Cuenta;
import com.banco.banca.domain.entity.EstadoCuenta;
import com.banco.banca.domain.entity.TipoCuenta;
import com.banco.banca.domain.service.CuentaService;
import com.banco.banca.web.dto.*;
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
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
@Tag(name = "Cuentas", description = "Gestión de cuentas bancarias")
public class CuentaController {
    private final CuentaService cuentaService;

    @PostMapping
    @Operation(summary = "Crear cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaCreateRequest req){
        Cuenta creada = cuentaService.crear(req.getClienteId(), req.getTipoCuenta(), req.getExentaGmf(), req.getUsuarioPropietario());
        return ResponseEntity.status(HttpStatus.CREATED).body(CuentaResponse.fromEntity(creada));
    }

    @PostMapping("/con-saldo")
    @Operation(summary = "Crear cuenta con saldo inicial")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada")
    })
    public ResponseEntity<CuentaResponse> crearConSaldo(@Valid @RequestBody CuentaCreateWithSaldoRequest req){
        Cuenta creada = cuentaService.crearConSaldo(req.getClienteId(), req.getTipoCuenta(), req.getSaldoInicial(), req.getExentaGmf(), req.getUsuarioPropietario());
        return ResponseEntity.status(HttpStatus.CREATED).body(CuentaResponse.fromEntity(creada));
    }

    @GetMapping
    @Operation(summary = "Listar cuentas", description = "Filtra por cliente, tipo, estado y número de cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de cuentas")
    })
    public List<CuentaResponse> listar(
            @RequestParam(name = "clienteId", required = false) UUID clienteId,
            @RequestParam(name = "tipoCuenta", required = false) TipoCuenta tipoCuenta,
            @RequestParam(name = "estado", required = false) EstadoCuenta estado,
            @RequestParam(name = "numeroCuenta", required = false) String numeroCuenta
    ){
        return cuentaService.buscar(clienteId, tipoCuenta, estado, numeroCuenta)
                .stream().map(CuentaResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cuenta por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public CuentaDetalleResponse obtener(@PathVariable(name = "id") UUID id){
        Cuenta cuenta = cuentaService.obtener(id);
        var movimientos = cuentaService.ultimosMovimientos(id, 10).stream()
                .map(MovimientoResponse::fromEntity)
                .toList();
        return CuentaDetalleResponse.fromEntity(cuenta, movimientos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public CuentaResponse actualizarEstado(@PathVariable(name = "id") UUID id, @Valid @RequestBody CuentaUpdateEstadoRequest req){
        Cuenta actualizada = cuentaService.actualizarEstado(id, req.getEstado());
        return CuentaResponse.fromEntity(actualizada);
    }
}

