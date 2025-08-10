-- V3: Convertir columnas ENUM de PostgreSQL a VARCHAR para compatibilidad JPA (EnumType.STRING)
-- Motivo: Error "column is of type <enum> but expression is of type character varying"
-- Estrategia: castear el valor del enum a texto y cambiar el tipo de columna a VARCHAR(20)

-- cuentas
ALTER TABLE cuentas
    ALTER COLUMN estado TYPE VARCHAR(20) USING estado::text,
    ALTER COLUMN tipo_cuenta TYPE VARCHAR(20) USING tipo_cuenta::text;

-- transacciones
ALTER TABLE transacciones
    ALTER COLUMN tipo TYPE VARCHAR(20) USING tipo::text,
    ALTER COLUMN estado TYPE VARCHAR(20) USING estado::text;

-- movimientos
ALTER TABLE movimientos
    ALTER COLUMN tipo_mov TYPE VARCHAR(20) USING tipo_mov::text;

