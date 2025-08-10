-- V2: Ajuste tipo de columna numero_cuenta a VARCHAR(10)
-- Motivo: Hibernate valida VARCHAR(10) pero el esquema inicial creó CHAR(10)
-- Nota: Se usa TRIM para remover posibles espacios en valores existentes

ALTER TABLE cuentas
    ALTER COLUMN numero_cuenta TYPE VARCHAR(10) USING TRIM(numero_cuenta);

