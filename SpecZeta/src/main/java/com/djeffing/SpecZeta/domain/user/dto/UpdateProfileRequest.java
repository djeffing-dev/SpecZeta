package com.djeffing.SpecZeta.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "UpdateProfileRequest",
        description = "Payload de mise à jour partielle du profil de l'utilisateur connecté. "
                + "Tous les champs sont optionnels : seuls les champs non `null` sont modifiés."
)
public class UpdateProfileRequest {

    @Size(min = 3, max = 80)
    @Schema(description = "Nouveau pseudo.", example = "JaneTheGeek",
            minLength = 3, maxLength = 80,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String pseudo;

    @Size(max = 120)
    @Schema(description = "Nouvelle ville.", example = "Paris", maxLength = 120,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String ville;

    @Size(max = 30)
    @Schema(description = "Numéro de téléphone.", example = "+33 6 12 34 56 78", maxLength = 30,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String telephone;

    @Size(max = 500)
    @Schema(description = "URL de la photo de profil.",
            example = "https://dl.dropboxusercontent.com/s/abc123/avatar.jpg",
            maxLength = 500,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            format = "uri")
    private String photoUrl;

    @Size(max = 2000)
    @Schema(description = "Texte libre de présentation affiché sur le profil public.",
            example = "Vendeur expérimenté, matériel testé et certifié.",
            maxLength = 2000,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String biographie;

    @Schema(description = "Date de naissance (ISO-8601).", example = "1992-08-15",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate dateNaissance;

    @Size(max = 200)
    @Schema(description = "Adresse postale.", example = "12 rue des Lilas, Apt 4", maxLength = 200,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String adresse;

    @Size(max = 10)
    @Schema(description = "Code postal.", example = "69003", maxLength = 10,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String codePostal;

    @Size(max = 100)
    @Schema(description = "Pays.", example = "France", maxLength = 100,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String pays;

    @Size(max = 255)
    @Schema(description = "Site web personnel.", example = "https://janedoe.dev",
            maxLength = 255, format = "uri",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String siteWeb;
}
