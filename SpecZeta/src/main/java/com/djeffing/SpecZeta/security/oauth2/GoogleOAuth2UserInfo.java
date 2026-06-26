package com.djeffing.SpecZeta.security.oauth2;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    /**
     * Stocke les attributs renvoyés par Google (champs {@code sub}, {@code email},
     * {@code name}, {@code picture}…).
     */
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    /** Google renvoie l'identifiant utilisateur dans le claim standard OIDC {@code sub}. */
    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    /** Email vérifié associé au compte Google. */
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    /** Nom complet (« prénom + nom ») affiché sur le compte Google. */
    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    /** URL directe de la photo de profil Google. */
    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}
