package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.common.exception.NotFoundException;
import com.banco.banca.domain.entity.*;
import com.banco.banca.domain.repository.CuentaRepository;
import com.banco.banca.domain.repository.MovimientoRepository;
import com.banco.banca.domain.repository.TransaccionRepository;
import com.banco.banca.web.dto.TransaccionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransaccionService {
    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final MovimientoRepository movimientoRepository;

    @Transactional
    public TransaccionResponse consignar(UUID cuentaDestinoId, BigDecimal monto, String descripcion, String usuario) {
        validarMonto(monto);
        
        var cuenta = cuentaRepository.findWithLockingById(cuentaDestinoId)
                .orElseThrow(() -> new NotFoundException("Cuenta destino no encontrada"));
        
        validarCuentaActiva(cuenta);

        BigDecimal saldoAntes = cuenta.getSaldo();
        cuenta.setSaldo(saldoAntes.add(monto));

        Transaccion tx = Transaccion.builder()
                .tipo(TipoTransaccion.CONSIGNACION)
                .monto(monto)
                .descripcion(descripcion)
                .estado(EstadoTransaccion.OK)
                .creadoPor(usuario)
                .cuentaDestino(cuenta)
                .build();
        transaccionRepository.save(tx);

        var mov = Movimiento.builder()
                .transaccion(tx)
                .cuenta(cuenta)
                .tipoMov(TipoMovimiento.CREDIT)
                .monto(monto)
                .saldoAntes(saldoAntes)
                .saldoDespues(cuenta.getSaldo())
                .build();
        movimientoRepository.save(mov);
        cuentaRepository.save(cuenta); // Guardar cuenta explícitamente
        var resp = new TransaccionResponse();
        resp.setId(tx.getId());
        resp.setTipo(tx.getTipo());
        resp.setFecha(tx.getFecha());
        resp.setCuentaDestinoId(cuenta.getId());
        resp.setMonto(tx.getMonto());
        resp.setDescripcion(tx.getDescripcion());
        resp.setReferencia(tx.getReferencia());
        resp.setEstado(tx.getEstado());
        resp.setCreadoPor(tx.getCreadoPor());
        return resp;
    }

    @Transactional
    public TransaccionResponse retirar(UUID cuentaOrigenId, BigDecimal monto, String descripcion, String usuario) {
        validarMonto(monto);
        
        var cuenta = cuentaRepository.findWithLockingById(cuentaOrigenId)
                .orElseThrow(() -> new NotFoundException("Cuenta origen no encontrada"));
        
        validarCuentaActiva(cuenta);

        if (cuenta.getTipoCuenta() == TipoCuenta.AHORROS && cuenta.getSaldo().compareTo(monto) < 0) {
            throw new BusinessException("Fondos insuficientes en cuenta de ahorros", HttpStatus.CONFLICT);
        }
        
        // Para cuentas corrientes, permitir saldo negativo según política del banco
        // pero validar que no exceda límites establecidos
        if (cuenta.getTipoCuenta() == TipoCuenta.CORRIENTE) {
            BigDecimal saldoDespues = cuenta.getSaldo().subtract(monto);
            // Aquí se podría agregar validación de límite de sobregiro
            if (saldoDespues.compareTo(new BigDecimal("-1000000")) < 0) { // Ejemplo: límite de 1M
                throw new BusinessException("Monto excede el límite de sobregiro permitido", HttpStatus.CONFLICT);
            }
        }
        
        BigDecimal saldoAntes = cuenta.getSaldo();
        cuenta.setSaldo(saldoAntes.subtract(monto));

        Transaccion tx = Transaccion.builder()
                .tipo(TipoTransaccion.RETIRO)
                .monto(monto)
                .descripcion(descripcion)
                .estado(EstadoTransaccion.OK)
                .creadoPor(usuario)
                .cuentaOrigen(cuenta)
                .build();
        transaccionRepository.save(tx);

        var mov = Movimiento.builder()
                .transaccion(tx)
                .cuenta(cuenta)
                .tipoMov(TipoMovimiento.DEBIT)
                .monto(monto)
                .saldoAntes(saldoAntes)
                .saldoDespues(cuenta.getSaldo())
                .build();
        movimientoRepository.save(mov);
        cuentaRepository.save(cuenta); // Guardar cuenta explícitamente
        var resp = new TransaccionResponse();
        resp.setId(tx.getId());
        resp.setTipo(tx.getTipo());
        resp.setFecha(tx.getFecha());
        resp.setCuentaOrigenId(cuenta.getId());
        resp.setMonto(tx.getMonto());
        resp.setDescripcion(tx.getDescripcion());
        resp.setReferencia(tx.getReferencia());
        resp.setEstado(tx.getEstado());
        resp.setCreadoPor(tx.getCreadoPor());
        return resp;
    }

    @Transactional
    public TransaccionResponse transferir(UUID cuentaOrigenId, UUID cuentaDestinoId, BigDecimal monto, String descripcion, String usuario) {
        if (cuentaOrigenId.equals(cuentaDestinoId)) {
            throw new BusinessException("Cuenta origen y destino no pueden ser iguales", HttpStatus.BAD_REQUEST);
        }
        
        validarMonto(monto);
        
        // ordenar para evitar deadlocks
        var firstId = Comparator.<UUID>naturalOrder().compare(cuentaOrigenId, cuentaDestinoId) <= 0 ? cuentaOrigenId : cuentaDestinoId;
        var secondId = firstId.equals(cuentaOrigenId) ? cuentaDestinoId : cuentaOrigenId;

        var first = cuentaRepository.findWithLockingById(firstId).orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
        var second = cuentaRepository.findWithLockingById(secondId).orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));

        var origen = first.getId().equals(cuentaOrigenId) ? first : second;
        var destino = origen == first ? second : first;
        
        validarCuentaActiva(origen);
        validarCuentaActiva(destino);

        if (origen.getTipoCuenta() == TipoCuenta.AHORROS && origen.getSaldo().compareTo(monto) < 0) {
            throw new BusinessException("Fondos insuficientes en cuenta de ahorros", HttpStatus.CONFLICT);
        }
        
        // Para cuentas corrientes, validar límite de sobregiro
        if (origen.getTipoCuenta() == TipoCuenta.CORRIENTE) {
            BigDecimal saldoDespues = origen.getSaldo().subtract(monto);
            if (saldoDespues.compareTo(new BigDecimal("-1000000")) < 0) {
                throw new BusinessException("Monto excede el límite de sobregiro permitido", HttpStatus.CONFLICT);
            }
        }
        
        BigDecimal saldoAntesOrigen = origen.getSaldo();
        BigDecimal saldoAntesDestino = destino.getSaldo();

        origen.setSaldo(saldoAntesOrigen.subtract(monto));
        destino.setSaldo(saldoAntesDestino.add(monto));

        Transaccion tx = Transaccion.builder()
                .tipo(TipoTransaccion.TRANSFERENCIA)
                .monto(monto)
                .descripcion(descripcion)
                .estado(EstadoTransaccion.OK)
                .creadoPor(usuario)
                .cuentaOrigen(origen)
                .cuentaDestino(destino)
                .build();
        transaccionRepository.save(tx);

        var movDeb = Movimiento.builder()
                .transaccion(tx)
                .cuenta(origen)
                .tipoMov(TipoMovimiento.DEBIT)
                .monto(monto)
                .saldoAntes(saldoAntesOrigen)
                .saldoDespues(origen.getSaldo())
                .build();
        var movCred = Movimiento.builder()
                .transaccion(tx)
                .cuenta(destino)
                .tipoMov(TipoMovimiento.CREDIT)
                .monto(monto)
                .saldoAntes(saldoAntesDestino)
                .saldoDespues(destino.getSaldo())
                .build();
        movimientoRepository.save(movDeb);
        movimientoRepository.save(movCred);
        cuentaRepository.save(origen); // Guardar cuentas explícitamente
        cuentaRepository.save(destino);
        var resp = new TransaccionResponse();
        resp.setId(tx.getId());
        resp.setTipo(tx.getTipo());
        resp.setFecha(tx.getFecha());
        resp.setCuentaOrigenId(origen.getId());
        resp.setCuentaDestinoId(destino.getId());
        resp.setMonto(tx.getMonto());
        resp.setDescripcion(tx.getDescripcion());
        resp.setReferencia(tx.getReferencia());
        resp.setEstado(tx.getEstado());
        resp.setCreadoPor(tx.getCreadoPor());
        return resp;
    }
    
    @Transactional(readOnly = true)
    public List<TransaccionResponse> buscar(UUID cuentaId, Instant fechaDesde, Instant fechaHasta) {
        Specification<Transaccion> spec = Specification.where(null);
        if (cuentaId != null) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("cuentaOrigen").get("id"), cuentaId),
                    cb.equal(root.get("cuentaDestino").get("id"), cuentaId)
            ));
        }
        if (fechaDesde != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
        }
        if (fechaHasta != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
        }
        var list = transaccionRepository.findAll(spec, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "fecha"));
        return list.stream().map(TransaccionResponse::fromEntity).collect(Collectors.toList());
    }
    
    private void validarMonto(BigDecimal monto) {
        if (monto == null) {
            throw new BusinessException("Monto es requerido", HttpStatus.BAD_REQUEST);
        }
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Monto debe ser mayor a 0", HttpStatus.BAD_REQUEST);
        }
        if (monto.compareTo(new BigDecimal("999999999999.99")) > 0) {
            throw new BusinessException("Monto excede el límite máximo permitido", HttpStatus.BAD_REQUEST);
        }
    }
    
    private void validarCuentaActiva(Cuenta cuenta) {
        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new BusinessException("La cuenta debe estar activa para realizar transacciones", HttpStatus.CONFLICT);
        }
    }
}

