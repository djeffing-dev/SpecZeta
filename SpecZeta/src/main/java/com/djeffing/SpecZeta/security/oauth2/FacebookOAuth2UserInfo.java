package com.djeffing.SpecZeta.security.oauth2;

import java.util.Map;

public class FacebookOAuth2UserInfo extends OAuth2UserInfo {

    /**
     * Stocke les attributs renvoyés par la Graph API Facebook
     * (champs {@code id}, {@code email}, {@code name}, {@code picture.data.url}…).
     */
    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    /** Identifiant numérique stable du compte Facebook. */
    @Override
    public String getId() {
        return (String) attributes.get("id");
    }

    /** Email du compte (nécessite le scope {@code email} autorisé par l'utilisateur). */
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    /** Nom complet renvoyé par la Graph API. */
    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    /**
     * Déballe la structure imbriquée {@code picture.data.url} de la Graph API
     * et renvoie l'URL directe de l'avatar. Retourne {@code null} si la structure
     * attendue n'est pas présente.
     */
    @Override
    @SuppressWarnings("unchecked")
    public String getImageUrl() {
        Object picture = attributes.get("picture");
        if (picture instanceof Map<?, ?> pictureMap) {
            Object data = pictureMap.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                return (String) dataMap.get("url");
            }
        }
        return null;
    }
}
