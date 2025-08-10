-- Flyway V1: initial schema for clientes, cuentas, transacciones, movimientos and helper sequence/types

-- Enums (PostgreSQL); if using other DBs, replace with VARCHAR + CHECKs
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_cuenta') THEN
        CREATE TYPE tipo_cuenta AS ENUM ('AHORROS','CORRIENTE');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_cuenta') THEN
        CREATE TYPE estado_cuenta AS ENUM ('ACTIVA','INACTIVA','CANCELADA');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_tx') THEN
        CREATE TYPE tipo_tx AS ENUM ('CONSIGNACION','RETIRO','TRANSFERENCIA');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_tx') THEN
        CREATE TYPE estado_tx AS ENUM ('OK','FAILED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_mov') THEN
        CREATE TYPE tipo_mov AS ENUM ('DEBIT','CREDIT');
    END IF;
END $$;

-- Sequence for account numbers (last 8 digits); prefijo (53/33) se concatena en app
CREATE SEQUENCE IF NOT EXISTS seq_cuentas START 1 INCREMENT 1;

CREATE TABLE IF NOT EXISTS clientes (
    id UUID PRIMARY KEY,
    tipo_identificacion VARCHAR(10) NOT NULL,
    num_identificacion VARCHAR(50) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    fecha_nacimiento DATE NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    fecha_modificacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS cuentas (
    id UUID PRIMARY KEY,
    cliente_id UUID NOT NULL REFERENCES clientes(id),
    tipo_cuenta tipo_cuenta NOT NULL,
    numero_cuenta CHAR(10) NOT NULL UNIQUE,
    estado estado_cuenta NOT NULL,
    saldo NUMERIC(18,2) NOT NULL,
    exenta_gmf BOOLEAN NOT NULL DEFAULT FALSE,
    usuario_propietario VARCHAR(100),
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    fecha_modificacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_cuentas_cliente ON cuentas(cliente_id);

CREATE TABLE IF NOT EXISTS transacciones (
    id UUID PRIMARY KEY,
    tipo tipo_tx NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    cuenta_origen_id UUID NULL REFERENCES cuentas(id),
    cuenta_destino_id UUID NULL REFERENCES cuentas(id),
    monto NUMERIC(18,2) NOT NULL,
    descripcion VARCHAR(255),
    referencia VARCHAR(100),
    estado estado_tx NOT NULL,
    creado_por VARCHAR(100)
);
CREATE INDEX IF NOT EXISTS idx_tx_origen ON transacciones(cuenta_origen_id);
CREATE INDEX IF NOT EXISTS idx_tx_destino ON transacciones(cuenta_destino_id);
CREATE INDEX IF NOT EXISTS idx_tx_fecha ON transacciones(fecha);

CREATE TABLE IF NOT EXISTS movimientos (
    id UUID PRIMARY KEY,
    transaccion_id UUID NOT NULL REFERENCES transacciones(id),
    cuenta_id UUID NOT NULL REFERENCES cuentas(id),
    tipo_mov tipo_mov NOT NULL,
    monto NUMERIC(18,2) NOT NULL,
    saldo_antes NUMERIC(18,2) NOT NULL,
    saldo_despues NUMERIC(18,2) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_mov_cuenta ON movimientos(cuenta_id);
CREATE INDEX IF NOT EXISTS idx_mov_tx ON movimientos(transaccion_id);

