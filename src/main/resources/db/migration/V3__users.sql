-- V3__users.sql
-- Tabela de usuários para autenticação e autorização

CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100)  NOT NULL,
    email      VARCHAR(150)  NOT NULL,
    password   VARCHAR(255)  NOT NULL,
    role       VARCHAR(20)   NOT NULL DEFAULT 'CUSTOMER',
    active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'CUSTOMER'))
);

CREATE INDEX idx_users_email ON users (email);

-- Usuário admin padrão (senha: Admin@123 — troque em produção!)
-- Hash BCrypt gerado com strength 12
INSERT INTO users (name, email, password, role)
VALUES (
    'Admin',
    'admin@shopfy.com',
    '$2a$12$5XTRbMBEBFRp7hVtHEJt9.V8V8VzGH6kDcvTBW6r3JhMmEAGdQmcG',
    'ADMIN'
);
