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

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {
    private final TransaccionService transaccionService;

    private String currentUser(String header){
        return header != null && !header.isBlank() ? header : "system";
    }

    @PostMapping("/consignacion")
    public ResponseEntity<TransaccionResponse> consignar(@RequestHeader(value="X-User", required=false) String user,
                                                 @Valid @RequestBody ConsignacionRequest req){
        TransaccionResponse tx = transaccionService.consignar(req.getCuentaDestinoId(), req.getMonto(), req.getDescripcion(), currentUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PostMapping("/retiro")
    public ResponseEntity<TransaccionResponse> retirar(@RequestHeader(value="X-User", required=false) String user,
                                               @Valid @RequestBody RetiroRequest req){
        TransaccionResponse tx = transaccionService.retirar(req.getCuentaOrigenId(), req.getMonto(), req.getDescripcion(), currentUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PostMapping("/transferencia")
    public ResponseEntity<TransaccionResponse> transferir(@RequestHeader(value="X-User", required=false) String user,
                                                  @Valid @RequestBody TransferenciaRequest req){
        TransaccionResponse tx = transaccionService.transferir(req.getCuentaOrigenId(), req.getCuentaDestinoId(), req.getMonto(), req.getDescripcion(), currentUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @GetMapping
    public List<TransaccionResponse> listar(
            @RequestParam(name = "cuenta", required = false) UUID cuenta,
            @RequestParam(name = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fechaHasta
    ){
        return transaccionService.buscar(cuenta, fechaDesde, fechaHasta);
    }
}

