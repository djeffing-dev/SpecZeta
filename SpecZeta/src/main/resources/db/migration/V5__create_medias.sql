CREATE TABLE annonce_medias (
    id            BIGSERIAL PRIMARY KEY,
    annonce_id    BIGINT        NOT NULL,
    dropbox_url   VARCHAR(1000) NOT NULL,
    dropbox_path  VARCHAR(500)  NOT NULL,
    ordre         INTEGER       NOT NULL DEFAULT 0,
    uploaded_at   TIMESTAMP     NOT NULL,
    CONSTRAINT fk_medias_annonce FOREIGN KEY (annonce_id) REFERENCES annonces (id) ON DELETE CASCADE
);

CREATE INDEX idx_medias_annonce ON annonce_medias (annonce_id);
