package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.common.exception.NotFoundException;
import com.banco.banca.domain.entity.Cliente;
import com.banco.banca.domain.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;
    
    // Patrón para validar email más estricto
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    public List<Cliente> listar(){
        return clienteRepository.findAll();
    }

    public List<Cliente> buscar(String tipoIdentificacion, String numIdentificacion, String nombre, String apellido, String email){
        return clienteRepository.search(
                blankToNull(tipoIdentificacion),
                blankToNull(numIdentificacion),
                blankToNull(nombre),
                blankToNull(apellido),
                blankToNull(email)
        );
    }

    public Cliente obtener(UUID id){
        return clienteRepository.findById(id).orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
    }

    @Transactional
    public Cliente crear(Cliente cliente){
        validarCliente(cliente);
        
        if (clienteRepository.existsByTipoIdentificacionAndNumIdentificacion(
                cliente.getTipoIdentificacion(), cliente.getNumIdentificacion())){
            throw new BusinessException("Ya existe un cliente con el mismo tipo y número de identificación", HttpStatus.CONFLICT);
        }
        
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizar(UUID id, Cliente cambios){
        Cliente existente = obtener(id);
        
        // Validar edad en actualización
        if (cambios.getFechaNacimiento() != null) {
            int edad = Period.between(cambios.getFechaNacimiento(), LocalDate.now()).getYears();
            if (edad < 18) {
                throw new BusinessException("No se puede actualizar cliente a menor de edad", HttpStatus.BAD_REQUEST);
            }
        }
        
        // Validar unicidad compuesta solo si cambió la identificación
        if (!existente.getTipoIdentificacion().equals(cambios.getTipoIdentificacion()) ||
            !existente.getNumIdentificacion().equals(cambios.getNumIdentificacion())) {
            
            if (clienteRepository.existsByTipoIdentificacionAndNumIdentificacion(
                    cambios.getTipoIdentificacion(), cambios.getNumIdentificacion())){
                throw new BusinessException("Ya existe un cliente con el mismo tipo y número de identificación", HttpStatus.CONFLICT);
            }
        }
        
        validarCliente(cambios);
        
        existente.setTipoIdentificacion(cambios.getTipoIdentificacion());
        existente.setNumIdentificacion(cambios.getNumIdentificacion());
        existente.setNombres(cambios.getNombres());
        existente.setApellidos(cambios.getApellidos());
        existente.setEmail(cambios.getEmail());
        existente.setFechaNacimiento(cambios.getFechaNacimiento());
        return clienteRepository.save(existente);
    }
    
    @Transactional
    public void eliminar(UUID id) {
        Cliente cliente = obtener(id);
        
        if (clienteRepository.hasCuentasAsociadas(id)) {
            throw new BusinessException("No se puede eliminar cliente con cuentas asociadas", HttpStatus.CONFLICT);
        }
        
        clienteRepository.delete(cliente);
    }
    
    private void validarCliente(Cliente cliente) {
        if (cliente.getFechaNacimiento() == null) {
            throw new BusinessException("fechaNacimiento es requerida", HttpStatus.BAD_REQUEST);
        }
        
        int edad = Period.between(cliente.getFechaNacimiento(), LocalDate.now()).getYears();
        if (edad < 18) {
            throw new BusinessException("No se puede crear cliente menor de edad", HttpStatus.BAD_REQUEST);
        }
        
        if (cliente.getNombres() == null || cliente.getNombres().trim().length() < 2) {
            throw new BusinessException("Nombres deben tener al menos 2 caracteres", HttpStatus.BAD_REQUEST);
        }
        
        if (cliente.getApellidos() == null || cliente.getApellidos().trim().length() < 2) {
            throw new BusinessException("Apellidos deben tener al menos 2 caracteres", HttpStatus.BAD_REQUEST);
        }
        
        if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(cliente.getEmail()).matches()) {
                throw new BusinessException("Formato de email inválido", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }
}

