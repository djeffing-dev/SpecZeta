package com.djeffing.SpecZeta.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "OTPrequest",
        description = "Payload de vérification d'un code OTP. Utilisé par `POST /api/otp/verify-otp`."
)
public class OTPrequest {

    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Adresse email du compte à vérifier.",
            example = "jane.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Le code de vérification est requis")
    @Pattern(regexp = "\\d{6}", message = "Le code doit contenir exactement 6 chiffres")
    @Schema(description = "Code OTP à 6 chiffres reçu par email.",
            example = "123456",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String plainCode;
}
