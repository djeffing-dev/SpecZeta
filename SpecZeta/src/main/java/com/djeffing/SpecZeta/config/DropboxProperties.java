package com.djeffing.SpecZeta.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "dropbox")
public class DropboxProperties {

    /** Clé applicative Dropbox (depuis l'app console). Sert au refresh du token. */
    @NotBlank
    private String appKey;

    /** Secret applicatif Dropbox. Combiné avec {@code appKey} pour le refresh. */
    @NotBlank
    private String appSecret;

    /**
     * Access token actuel. Considéré comme une valeur initiale : il sera refreshé
     * automatiquement quand Dropbox renvoie 401 (expiration au bout de ~4h).
     */
    @NotBlank
    private String accessToken;

    /**
     * Refresh token long-vie utilisé pour obtenir un nouvel access token quand
     * le précédent expire. Obtenu via le flow OAuth2 lors de l'enrôlement initial.
     */
    @NotBlank
    private String refreshToken;

    /**
     * Environnement applicatif injecté dans le path Dropbox pour isoler
     * les fichiers : {@code /marketplace/{environment}/annonces/...}.
     * Valeurs typiques : {@code dev}, {@code staging}, {@code prod}.
     */
    @NotBlank
    private String environment = "dev";

    /** Nombre maximal de tentatives sur erreurs 5xx ou timeout réseau. */
    @Min(1)
    private int maxRetries = 3;

    /** Timeout d'établissement de connexion ET de lecture, en millisecondes. */
    @Min(1000)
    private long timeoutMs = 30_000;

    /**
     * Préfixe stable utilisé par le service pour construire les paths Dropbox.
     * Volontairement non configurable : isole les fichiers de la marketplace
     * d'autres usages éventuels du même compte Dropbox.
     */
    public String getBasePath() {
        return "/marketplace";
    }
}
