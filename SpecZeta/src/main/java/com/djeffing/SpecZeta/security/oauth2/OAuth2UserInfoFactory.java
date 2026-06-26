package com.djeffing.SpecZeta.security.oauth2;

import com.djeffing.SpecZeta.domain.user.enums.AuthProvider;
import com.djeffing.SpecZeta.shared.exception.BadRequestException;

import java.util.Locale;
import java.util.Map;

public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    /**
     * Choisit l'implémentation concrète de {@link OAuth2UserInfo} en fonction du
     * {@code registrationId} de Spring Security (« google », « facebook »).
     * Permet d'ajouter de nouveaux providers en ne touchant qu'à cette méthode.
     *
     * @param registrationId identifiant déclaré dans {@code spring.security.oauth2.client.registration}
     * @param attributes     map d'attributs renvoyée par le provider
     * @throws BadRequestException si le provider n'est pas pris en charge
     */
    public static OAuth2UserInfo from(String registrationId, Map<String, Object> attributes) {
        AuthProvider provider;
        try {
            provider = AuthProvider.valueOf(registrationId.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Provider OAuth2 non supporté : " + registrationId);
        }

        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case FACEBOOK -> new FacebookOAuth2UserInfo(attributes);
            case LOCAL -> throw new BadRequestException(
                    "Le provider LOCAL ne peut pas être utilisé via OAuth2");
        };
    }
}
