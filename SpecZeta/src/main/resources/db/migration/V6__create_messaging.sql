CREATE TABLE conversations (
    id              BIGSERIAL PRIMARY KEY,
    annonce_id      BIGINT     NOT NULL,
    acheteur_id     BIGINT     NOT NULL,
    vendeur_id      BIGINT     NOT NULL,
    last_message_at TIMESTAMP,
    created_at      TIMESTAMP  NOT NULL,
    CONSTRAINT fk_conv_annonce  FOREIGN KEY (annonce_id)  REFERENCES annonces (id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_acheteur FOREIGN KEY (acheteur_id) REFERENCES users    (id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_vendeur  FOREIGN KEY (vendeur_id)  REFERENCES users    (id) ON DELETE CASCADE,
    CONSTRAINT uk_conv_annonce_acheteur UNIQUE (annonce_id, acheteur_id)
);

CREATE INDEX idx_conv_acheteur     ON conversations (acheteur_id);
CREATE INDEX idx_conv_vendeur      ON conversations (vendeur_id);
CREATE INDEX idx_conv_last_message ON conversations (last_message_at);

CREATE TABLE messages (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT    NOT NULL,
    expediteur_id   BIGINT    NOT NULL,
    contenu         TEXT      NOT NULL,
    lu              BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL,
    CONSTRAINT fk_msg_conv       FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_expediteur FOREIGN KEY (expediteur_id)   REFERENCES users         (id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_conversation ON messages (conversation_id);
CREATE INDEX idx_messages_created      ON messages (created_at);
