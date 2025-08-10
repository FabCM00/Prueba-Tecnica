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

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {
    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaCreateRequest req){
        Cuenta creada = cuentaService.crear(req.getClienteId(), req.getTipoCuenta(), req.getExentaGmf(), req.getUsuarioPropietario());
        return ResponseEntity.status(HttpStatus.CREATED).body(CuentaResponse.fromEntity(creada));
    }

    @PostMapping("/con-saldo")
    public ResponseEntity<CuentaResponse> crearConSaldo(@Valid @RequestBody CuentaCreateWithSaldoRequest req){
        Cuenta creada = cuentaService.crearConSaldo(req.getClienteId(), req.getTipoCuenta(), req.getSaldoInicial(), req.getExentaGmf(), req.getUsuarioPropietario());
        return ResponseEntity.status(HttpStatus.CREATED).body(CuentaResponse.fromEntity(creada));
    }

    @GetMapping
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
    public CuentaDetalleResponse obtener(@PathVariable(name = "id") UUID id){
        Cuenta cuenta = cuentaService.obtener(id);
        var movimientos = cuentaService.ultimosMovimientos(id, 10).stream()
                .map(MovimientoResponse::fromEntity)
                .toList();
        return CuentaDetalleResponse.fromEntity(cuenta, movimientos);
    }

    @PutMapping("/{id}")
    public CuentaResponse actualizarEstado(@PathVariable(name = "id") UUID id, @Valid @RequestBody CuentaUpdateEstadoRequest req){
        Cuenta actualizada = cuentaService.actualizarEstado(id, req.getEstado());
        return CuentaResponse.fromEntity(actualizada);
    }
}

