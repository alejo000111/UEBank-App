
--Tabla usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id      SERIAL PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    hash    TEXT        NOT NULL,
    creado  TIMESTAMPTZ NOT NULL DEFAULT now()
);

--Tabla cuentas
CREATE TABLE IF NOT EXISTS cuentas (
    id      SERIAL PRIMARY KEY,
    usuario VARCHAR(50)   NOT NULL,
    numero  VARCHAR(20)   NOT NULL UNIQUE,
    tipo    VARCHAR(10)   NOT NULL DEFAULT 'AHORROS' CHECK (tipo IN ('AHORROS', 'CORRIENTE')),
    saldo   NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (saldo >= 0),
    creada  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

--Tabla movimientos
CREATE TABLE IF NOT EXISTS movimientos (
    id          SERIAL PRIMARY KEY,
    cuenta_id   INTEGER       NOT NULL REFERENCES cuentas(id) ON DELETE CASCADE,
    tipo        VARCHAR(10)   NOT NULL CHECK (tipo IN ('DEPOSITO', 'RETIRO')),
    monto       NUMERIC(14,2) NOT NULL CHECK (monto > 0),
    descripcion TEXT,
    fecha       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

--Tabla metas
CREATE TABLE IF NOT EXISTS metas (
    id             SERIAL PRIMARY KEY,
    usuario        VARCHAR(50)   NOT NULL,
    nombre         VARCHAR(80)   NOT NULL,
    monto_objetivo NUMERIC(14,2) NOT NULL CHECK (monto_objetivo > 0),
    monto_ahorrado NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (monto_ahorrado >= 0),
    fecha_limite   DATE
);

--Índices
CREATE INDEX IF NOT EXISTS idx_cuentas_usuario ON cuentas(usuario);
CREATE INDEX IF NOT EXISTS idx_movimientos_cuenta ON movimientos(cuenta_id);
CREATE INDEX IF NOT EXISTS idx_metas_usuario ON metas(usuario);
