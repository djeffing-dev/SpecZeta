CREATE TABLE fiches_techniques (
    id                BIGSERIAL PRIMARY KEY,
    annonce_id        BIGINT       NOT NULL UNIQUE,
    modele            VARCHAR(150),
    marque            VARCHAR(100),
    processeur        VARCHAR(200),
    gpu               VARCHAR(200),
    ram_go            INTEGER,
    stockage_go       INTEGER,
    type_stockage     VARCHAR(30),
    ecran_taille      VARCHAR(30),
    ecran_resolution  VARCHAR(30),
    socket            VARCHAR(50),
    source_api        VARCHAR(30),
    raw_data_json     TEXT,
    CONSTRAINT fk_fiches_annonce FOREIGN KEY (annonce_id) REFERENCES annonces (id) ON DELETE CASCADE
);

CREATE INDEX idx_fiches_marque        ON fiches_techniques (marque);
CREATE INDEX idx_fiches_gpu           ON fiches_techniques (gpu);
CREATE INDEX idx_fiches_ram           ON fiches_techniques (ram_go);
CREATE INDEX idx_fiches_type_stockage ON fiches_techniques (type_stockage);
