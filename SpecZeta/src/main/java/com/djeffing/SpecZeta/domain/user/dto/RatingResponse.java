package com.djeffing.SpecZeta.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "RatingResponse",
        description = "Évaluation persistée renvoyée après création."
)
public class RatingResponse {

    @Schema(description = "Identifiant unique de l'évaluation.", example = "317")
    private Long id;

    @Schema(description = "Identifiant de l'utilisateur évalué.", example = "42")
    private Long evalueId;

    @Schema(description = "Identifiant de l'évaluateur.", example = "17")
    private Long evaluateurId;

    @Schema(description = "Pseudo de l'évaluateur (renvoyé pour affichage direct).", example = "JohnDoe")
    private String evaluateurPseudo;

    @Schema(description = "Identifiant de l'annonce ayant motivé l'évaluation (optionnel).", example = "1024")
    private Long annonceId;

    @Schema(description = "Note attribuée (1 à 5).", example = "5", minimum = "1", maximum = "5")
    private Integer note;

    @Schema(description = "Commentaire libre.",
            example = "Vendeur sérieux, matériel conforme.")
    private String commentaire;

    @Schema(description = "Horodatage de création.", example = "2026-06-03T10:18:42")
    private LocalDateTime createdAt;
}
