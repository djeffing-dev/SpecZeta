package com.djeffing.SpecZeta.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "UserRegistrationRequest",
        description = "Payload d'inscription d'un nouvel utilisateur via le formulaire local "
                + "(email + mot de passe). Utilisé par `POST /api/auth/register`."
)
public class UserRegistrationRequest {

    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Adresse email de l'utilisateur. Doit être unique dans le système.",
            example = "jane.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 255)
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, max = 100, message = "Le mot de passe doit faire entre 8 et 100 caractères")
    @Schema(description = "Mot de passe en clair. Stocké hashé en BCrypt côté serveur. "
            + "Longueur : 8 à 100 caractères.",
            example = "MotDePasseSecure!2026",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 8,
            maxLength = 100,
            format = "password")
    private String password;

    @NotBlank(message = "Le pseudo est requis")
    @Size(min = 3, max = 80, message = "Le pseudo doit faire entre 3 et 80 caractères")
    @Schema(description = "Pseudo affiché publiquement sur les annonces et la messagerie.",
            example = "JaneD",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 80)
    private String pseudo;

    @Size(max = 120)
    @Schema(description = "Ville de résidence (optionnel). Utilisée pour les recommandations et la géolocalisation des annonces.",
            example = "Lyon",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            maxLength = 120)
    private String ville;
}
