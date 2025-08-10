-- Flyway V4: Add unique constraints and additional validations

-- Agregar constraint único compuesto para tipo + número de identificación
ALTER TABLE clientes 
ADD CONSTRAINT uk_cliente_tipo_num_identificacion 
UNIQUE (tipo_identificacion, num_identificacion);

-- Remover constraint único simple de num_identificacion ya que ahora es compuesto
ALTER TABLE clientes 
DROP CONSTRAINT IF EXISTS clientes_num_identificacion_key;

-- Agregar constraint para validar que nombres y apellidos tengan al menos 2 caracteres
ALTER TABLE clientes 
ADD CONSTRAINT chk_nombres_min_length 
CHECK (LENGTH(TRIM(nombres)) >= 2);

ALTER TABLE clientes 
ADD CONSTRAINT chk_apellidos_min_length 
CHECK (LENGTH(TRIM(apellidos)) >= 2);

-- Agregar constraint para validar formato de email si no es null
ALTER TABLE clientes 
ADD CONSTRAINT chk_email_format 
CHECK (email IS NULL OR email ~ '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$');

-- Agregar constraint para validar que el cliente sea mayor de edad
ALTER TABLE clientes 
ADD CONSTRAINT chk_cliente_mayor_edad 
CHECK (fecha_nacimiento <= CURRENT_DATE - INTERVAL '18 years');

-- Agregar constraint para validar que el saldo no sea negativo en cuentas de ahorros
ALTER TABLE cuentas 
ADD CONSTRAINT chk_ahorros_saldo_no_negativo 
CHECK (NOT (tipo_cuenta = 'AHORROS' AND saldo < 0));

-- Agregar constraint para validar que el número de cuenta tenga el formato correcto
ALTER TABLE cuentas 
ADD CONSTRAINT chk_numero_cuenta_formato 
CHECK (
    (tipo_cuenta = 'AHORROS' AND numero_cuenta ~ '^53[0-9]{8}$') OR
    (tipo_cuenta = 'CORRIENTE' AND numero_cuenta ~ '^33[0-9]{8}$')
);

-- Agregar constraint para validar que el monto de transacciones sea positivo
ALTER TABLE transacciones 
ADD CONSTRAINT chk_monto_positivo 
CHECK (monto > 0);

-- Agregar constraint para validar que al menos una cuenta esté presente
ALTER TABLE transacciones 
ADD CONSTRAINT chk_cuenta_presente 
CHECK (cuenta_origen_id IS NOT NULL OR cuenta_destino_id IS NOT NULL);

-- Agregar constraint para validar que origen y destino sean diferentes en transferencias
ALTER TABLE transacciones 
ADD CONSTRAINT chk_origen_destino_diferentes 
CHECK (
    tipo != 'TRANSFERENCIA' OR 
    (cuenta_origen_id IS NOT NULL AND cuenta_destino_id IS NOT NULL AND cuenta_origen_id != cuenta_destino_id)
);

-- Agregar constraint para validar que el monto de movimientos sea positivo
ALTER TABLE movimientos 
ADD CONSTRAINT chk_movimiento_monto_positivo 
CHECK (monto > 0);

-- Crear índices para mejorar performance de consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_clientes_tipo_num_identificacion 
ON clientes(tipo_identificacion, num_identificacion);

CREATE INDEX IF NOT EXISTS idx_cuentas_estado 
ON cuentas(estado);

CREATE INDEX IF NOT EXISTS idx_transacciones_tipo_fecha 
ON transacciones(tipo, fecha);




