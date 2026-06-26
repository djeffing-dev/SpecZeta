-- Table principale des utilisateurs
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255),
    pseudo              VARCHAR(80)  NOT NULL UNIQUE,
    photo_url           VARCHAR(500),
    ville               VARCHAR(120),
    telephone           VARCHAR(30),
    provider            VARCHAR(20)  NOT NULL,
    provider_id         VARCHAR(150),
    email_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    rating_moyenne      DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    nombre_evaluations  INTEGER      NOT NULL DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT chk_users_provider CHECK (provider IN ('LOCAL', 'GOOGLE', 'FACEBOOK'))
);

CREATE INDEX idx_users_provider ON users (provider, provider_id);

-- Profils étendus
CREATE TABLE user_profiles (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL UNIQUE,
    biographie      TEXT,
    date_naissance  DATE,
    adresse         VARCHAR(200),
    code_postal     VARCHAR(10),
    pays            VARCHAR(100),
    site_web        VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
