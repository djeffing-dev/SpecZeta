package com.djeffing.SpecZeta.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "AuthResponse",
        description = "Réponse renvoyée lors d'une inscription ou d'une connexion réussie. "
                + "Contient le JWT à utiliser dans l'en-tête `Authorization` ainsi qu'un résumé de l'utilisateur."
)
public class AuthResponse {

    @Schema(description = "Jeton JWT signé. À transmettre dans l'en-tête `Authorization: Bearer <token>`.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0MiJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    private String accessToken;

    @Schema(description = "Jeton JWT signé. À transmettre lorsque le AssessToken a expirer pour en obtenir un nouveau.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0MiJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    private String refreshToken;

    @Schema(description = "Type de jeton renvoyé. Toujours `Bearer` pour cette API.",
            example = "Bearer",
            defaultValue = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Durée de validité du jeton en secondes.",
            example = "86400")
    private long expiresIn;

    @Schema(description = "Résumé public du compte authentifié.")
    private UserSummaryResponse user;
}
