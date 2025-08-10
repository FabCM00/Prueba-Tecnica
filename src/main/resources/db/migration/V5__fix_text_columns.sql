-- V5: Fix text columns in clientes if they were created as BYTEA by mistake
DO $$
BEGIN
    -- nombres
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'clientes' 
          AND column_name = 'nombres' 
          AND data_type = 'bytea'
    ) THEN
        EXECUTE 'ALTER TABLE clientes ALTER COLUMN nombres TYPE VARCHAR(100) USING convert_from(nombres, ''UTF8'')';
    END IF;

    -- apellidos
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'clientes' 
          AND column_name = 'apellidos' 
          AND data_type = 'bytea'
    ) THEN
        EXECUTE 'ALTER TABLE clientes ALTER COLUMN apellidos TYPE VARCHAR(100) USING convert_from(apellidos, ''UTF8'')';
    END IF;

    -- email
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'clientes' 
          AND column_name = 'email' 
          AND data_type = 'bytea'
    ) THEN
        EXECUTE 'ALTER TABLE clientes ALTER COLUMN email TYPE VARCHAR(255) USING convert_from(email, ''UTF8'')';
    END IF;
END$$; 