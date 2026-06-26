package com.djeffing.SpecZeta.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "LoginRequest",
        description = "Payload d'authentification locale par email/mot de passe. "
                + "Utilisé par `POST /api/auth/login`."
)
public class LoginRequest {

    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Adresse email du compte.",
            example = "jane.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Schema(description = "Mot de passe en clair (transmis via HTTPS).",
            example = "MotDePasseSecure!2026",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password")
    private String password;

    @NotNull(message = "Le rappel de connexion est requis")
    @Schema(description = "Rappel de connexion 'rememberMe' (transmis via HTTPS).",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean rememberMe;
}
