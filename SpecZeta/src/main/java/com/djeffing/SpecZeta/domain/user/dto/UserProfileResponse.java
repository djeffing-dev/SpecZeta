package com.djeffing.SpecZeta.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "UserProfileResponse",
        description = "Profil détaillé d'un utilisateur. Les champs sensibles (email, téléphone, "
                + "adresse précise) sont masqués lorsque le profil est consulté par un tiers."
)
public class UserProfileResponse {

    @Schema(description = "Identifiant unique de l'utilisateur.", example = "42")
    private Long id;

    @Schema(description = "Adresse email. Visible uniquement par le propriétaire du compte.",
            example = "jane.doe@example.com")
    private String email;

    @Schema(description = "Pseudo public.", example = "JaneD")
    private String pseudo;

    @Schema(description = "URL de la photo de profil.",
            example = "https://dl.dropboxusercontent.com/s/abc123/avatar.jpg")
    private String photoUrl;

    @Schema(description = "Ville déclarée par l'utilisateur.", example = "Lyon")
    private String ville;

    @Schema(description = "Numéro de téléphone. Visible uniquement par le propriétaire.",
            example = "+33 6 12 34 56 78")
    private String telephone;

    @Schema(description = "Fournisseur d'identité utilisé pour la création du compte.",
            example = "LOCAL",
            allowableValues = {"LOCAL", "GOOGLE", "FACEBOOK"})
    private String provider;

    @Schema(description = "`true` si l'email a été vérifié via le lien de confirmation.", example = "true")
    private Boolean emailVerified;

    @Schema(description = "Note moyenne reçue (1.0 à 5.0).", example = "4.6")
    private Double ratingMoyenne;

    @Schema(description = "Nombre d'évaluations reçues.", example = "18")
    private Integer nombreEvaluations;

    @Schema(description = "Date de création du compte (ISO-8601).", example = "2025-03-12T09:21:11")
    private LocalDateTime createdAt;

    @Schema(description = "Biographie / présentation libre affichée sur le profil public.",
            example = "Passionné de hardware depuis 15 ans, je revends mon matériel testé et certifié.",
            maxLength = 2000)
    private String biographie;

    @Schema(description = "Date de naissance (utilisée pour la vérification d'âge si requise).",
            example = "1992-08-15")
    private LocalDate dateNaissance;

    @Schema(description = "Adresse postale. Visible uniquement par le propriétaire.",
            example = "12 rue des Lilas, Apt 4")
    private String adresse;

    @Schema(description = "Code postal.", example = "69003")
    private String codePostal;

    @Schema(description = "Pays.", example = "France")
    private String pays;

    @Schema(description = "Site web personnel du vendeur (optionnel).",
            example = "https://janedoe.dev",
            format = "uri")
    private String siteWeb;
}
