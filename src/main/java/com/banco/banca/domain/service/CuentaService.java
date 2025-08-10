package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.common.exception.NotFoundException;
import com.banco.banca.domain.entity.*;
import com.banco.banca.domain.repository.ClienteRepository;
import com.banco.banca.domain.repository.CuentaRepository;
import com.banco.banca.domain.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CuentaService {
    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final MovimientoRepository movimientoRepository;

    @Transactional
    public Cuenta crear(UUID clienteId, TipoCuenta tipoCuenta, Boolean exentaGmf, String usuarioPropietario) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        
        Cuenta cuenta = Cuenta.builder()
                .cliente(cliente)
                .tipoCuenta(tipoCuenta)
                .numeroCuenta(generarNumeroCuentaUnico(tipoCuenta))
                .estado(EstadoCuenta.ACTIVA) // Estado inicial ACTIVA para ambos tipos
                .saldo(BigDecimal.ZERO)
                .exentaGmf(exentaGmf != null ? exentaGmf : false)
                .usuarioPropietario(usuarioPropietario)
                .build();
        return cuentaRepository.save(cuenta);
    }
    
    @Transactional
    public Cuenta crearConSaldo(UUID clienteId, TipoCuenta tipoCuenta, BigDecimal saldoInicial, Boolean exentaGmf, String usuarioPropietario) {
        if (saldoInicial == null || saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El saldo inicial no puede ser negativo", HttpStatus.BAD_REQUEST);
        }
        
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        
        Cuenta cuenta = Cuenta.builder()
                .cliente(cliente)
                .tipoCuenta(tipoCuenta)
                .numeroCuenta(generarNumeroCuentaUnico(tipoCuenta))
                .estado(EstadoCuenta.ACTIVA)
                .saldo(saldoInicial)
                .exentaGmf(exentaGmf != null ? exentaGmf : false)
                .usuarioPropietario(usuarioPropietario)
                .build();
        return cuentaRepository.save(cuenta);
    }

    @Transactional(readOnly = true)
    public Cuenta obtener(UUID id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
    }

    @Transactional
    public Cuenta actualizarEstado(UUID id, EstadoCuenta nuevoEstado) {
        Cuenta cuenta = obtener(id);
        
        if (nuevoEstado == EstadoCuenta.CANCELADA && cuenta.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Solo se puede cancelar cuentas con saldo 0", HttpStatus.CONFLICT);
        }
        
        if (nuevoEstado == EstadoCuenta.INACTIVA && cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("No se puede inactivar cuenta con saldo negativo", HttpStatus.CONFLICT);
        }
        
        cuenta.setEstado(nuevoEstado);
        return cuentaRepository.save(cuenta);
    }
    
    @Transactional(readOnly = true)
    public List<Movimiento> ultimosMovimientos(UUID cuentaId, int limit) {
        // Usamos método de repositorio para Top10. Si limit != 10, podríamos ampliar, pero para ahora soportamos 10.
        return movimientoRepository.findTop10ByCuenta_IdOrderByFechaDesc(cuentaId);
    }

    @Transactional(readOnly = true)
    public List<Cuenta> buscar(UUID clienteId, TipoCuenta tipoCuenta, EstadoCuenta estado, String numeroCuenta) {
        return cuentaRepository.search(clienteId, tipoCuenta, estado, numeroCuenta);
    }
    
    @Transactional
    public void validarSaldoNegativo(Cuenta cuenta, BigDecimal monto) {
        if (cuenta.getTipoCuenta() == TipoCuenta.AHORROS) {
            if (cuenta.getSaldo().subtract(monto).compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Las cuentas de ahorros no pueden tener saldo negativo", HttpStatus.CONFLICT);
            }
        }
        // Para cuentas corrientes, permitir saldo negativo según política del banco
    }

    private String generarNumeroCuentaUnico(TipoCuenta tipo) {
        String prefijo = (tipo == TipoCuenta.AHORROS) ? "53" : "33";
        String numeroCuenta;
        int intentos = 0;
        final int MAX_INTENTOS = 100;
        
        do {
            // Generar 8 dígitos aleatorios
            int numero = ThreadLocalRandom.current().nextInt(10000000, 100000000);
            numeroCuenta = prefijo + String.valueOf(numero);
            intentos++;
            
            if (intentos > MAX_INTENTOS) {
                throw new BusinessException("No se pudo generar un número de cuenta único", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } while (cuentaRepository.existsByNumeroCuenta(numeroCuenta));
        
        return numeroCuenta;
    }
}

