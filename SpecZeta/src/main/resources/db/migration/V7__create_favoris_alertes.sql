CREATE TABLE favoris (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    annonce_id BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_fav_user    FOREIGN KEY (user_id)    REFERENCES users    (id) ON DELETE CASCADE,
    CONSTRAINT fk_fav_annonce FOREIGN KEY (annonce_id) REFERENCES annonces (id) ON DELETE CASCADE,
    CONSTRAINT uk_favori_user_annonce UNIQUE (user_id, annonce_id)
);

CREATE INDEX idx_favoris_user    ON favoris (user_id);
CREATE INDEX idx_favoris_annonce ON favoris (annonce_id);

CREATE TABLE alertes_recherche (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT         NOT NULL,
    mots_cles    VARCHAR(255),
    prix_min     NUMERIC(12, 2),
    prix_max     NUMERIC(12, 2),
    categorie    VARCHAR(40),
    localisation VARCHAR(120),
    rayon_km     INTEGER,
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP      NOT NULL,
    updated_at   TIMESTAMP      NOT NULL,
    CONSTRAINT fk_alertes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_alertes_user   ON alertes_recherche (user_id);
CREATE INDEX idx_alertes_active ON alertes_recherche (active);
