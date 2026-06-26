package com.djeffing.SpecZeta.security.oauth2;

import java.util.Map;

public abstract class OAuth2UserInfo {

    protected final Map<String, Object> attributes;

    /**
     * Stocke la map d'attributs brute renvoyée par le provider OAuth2.
     * Les implémentations concrètes en extraient les informations utiles.
     */
    protected OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Expose les attributs bruts (utile pour les rattacher au {@code CustomUserPrincipal}).
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /** Identifiant unique chez le provider (Google : {@code sub}, Facebook : {@code id}). */
    public abstract String getId();

    /** Email vérifié renvoyé par le provider. */
    public abstract String getEmail();

    /** Nom d'affichage (utilisé pour générer un pseudo par défaut). */
    public abstract String getName();

    /** URL de l'avatar du compte (peut être {@code null}). */
    public abstract String getImageUrl();
}
