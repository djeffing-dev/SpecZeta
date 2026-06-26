CREATE TABLE annonces (
    id           BIGSERIAL PRIMARY KEY,
    vendeur_id   BIGINT          NOT NULL,
    categorie    VARCHAR(40)     NOT NULL,
    titre        VARCHAR(200)    NOT NULL,
    description  TEXT            NOT NULL,
    prix         NUMERIC(12, 2)  NOT NULL,
    etat         VARCHAR(20)     NOT NULL,
    mode_remise  VARCHAR(20)     NOT NULL,
    statut       VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    certifiee    BOOLEAN         NOT NULL DEFAULT FALSE,
    latitude     DOUBLE PRECISION,
    longitude    DOUBLE PRECISION,
    created_at   TIMESTAMP       NOT NULL,
    updated_at   TIMESTAMP       NOT NULL,
    CONSTRAINT fk_annonces_vendeur FOREIGN KEY (vendeur_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_annonces_statut CHECK (statut IN ('ACTIVE', 'VENDUE', 'EN_ATTENTE', 'SUSPENDUE')),
    CONSTRAINT chk_annonces_etat   CHECK (etat   IN ('NEUF', 'TRES_BON', 'BON', 'POUR_PIECES')),
    CONSTRAINT chk_annonces_remise CHECK (mode_remise IN ('MAIN_PROPRE', 'ENVOI_POSTAL', 'LES_DEUX')),
    CONSTRAINT chk_annonces_prix   CHECK (prix > 0)
);

CREATE INDEX idx_annonces_statut     ON annonces (statut);
CREATE INDEX idx_annonces_categorie  ON annonces (categorie);
CREATE INDEX idx_annonces_vendeur    ON annonces (vendeur_id);
CREATE INDEX idx_annonces_prix       ON annonces (prix);
CREATE INDEX idx_annonces_created_at ON annonces (created_at);
