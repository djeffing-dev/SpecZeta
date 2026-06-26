CREATE TABLE certifications (
    id                BIGSERIAL PRIMARY KEY,
    annonce_id        BIGINT       NOT NULL UNIQUE,
    type_benchmark    VARCHAR(30)  NOT NULL,
    url_benchmark     VARCHAR(1000),
    score_monocoeur   INTEGER,
    score_multicoeur  INTEGER,
    score_gpu         INTEGER,
    log_file_url      VARCHAR(1000),
    log_data_json     TEXT,
    verified_at       TIMESTAMP    NOT NULL,
    CONSTRAINT fk_certifs_annonce FOREIGN KEY (annonce_id) REFERENCES annonces (id) ON DELETE CASCADE,
    CONSTRAINT chk_certifs_type CHECK (type_benchmark IN ('GEEKBENCH', '3DMARK', 'CPUZ', 'HWINFO'))
);

CREATE INDEX idx_certifs_type ON certifications (type_benchmark);
