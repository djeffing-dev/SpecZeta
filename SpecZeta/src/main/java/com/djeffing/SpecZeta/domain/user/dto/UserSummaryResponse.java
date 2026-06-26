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
        name = "UserSummaryResponse",
        description = "Résumé public d'un utilisateur. Embarqué dans les annonces, conversations "
                + "et notifications pour éviter d'exposer le profil complet."
)
public class UserSummaryResponse {

    @Schema(description = "Identifiant unique de l'utilisateur.", example = "42")
    private Long id;

    @Schema(description = "Adresse email. Renvoyée uniquement pour l'utilisateur connecté lui-même.",
            example = "jane.doe@example.com")
    private String email;

    @Schema(description = "Pseudo public.", example = "JaneD")
    private String pseudo;

    @Schema(description = "URL de la photo de profil (Dropbox ou avatar OAuth2).",
            example = "https://dl.dropboxusercontent.com/s/abc123/avatar.jpg")
    private String photoUrl;

    @Schema(description = "Ville déclarée.", example = "Lyon")
    private String ville;

    @Schema(description = "Note moyenne reçue par l'utilisateur (de 1.0 à 5.0).", example = "4.6")
    private Double ratingMoyenne;

    @Schema(description = "Nombre total d'évaluations reçues.", example = "18")
    private Integer nombreEvaluations;
}
