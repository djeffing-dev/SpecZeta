CREATE TABLE user_ratings (
    id             BIGSERIAL PRIMARY KEY,
    evalue_id      BIGINT    NOT NULL,
    evaluateur_id  BIGINT    NOT NULL,
    annonce_id     BIGINT,
    note           INTEGER   NOT NULL,
    commentaire    TEXT,
    created_at     TIMESTAMP NOT NULL,
    CONSTRAINT fk_ratings_evalue      FOREIGN KEY (evalue_id)     REFERENCES users    (id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_evaluateur  FOREIGN KEY (evaluateur_id) REFERENCES users    (id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_annonce     FOREIGN KEY (annonce_id)    REFERENCES annonces (id) ON DELETE SET NULL,
    CONSTRAINT chk_ratings_note       CHECK (note BETWEEN 1 AND 5),
    CONSTRAINT chk_ratings_self       CHECK (evalue_id <> evaluateur_id),
    CONSTRAINT uk_rating_per_annonce_evaluateur UNIQUE (annonce_id, evaluateur_id)
);

CREATE INDEX idx_ratings_evalue      ON user_ratings (evalue_id);
CREATE INDEX idx_ratings_evaluateur  ON user_ratings (evaluateur_id);
