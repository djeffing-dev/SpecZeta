package com.djeffing.SpecZeta.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
        name = "RatingRequest",
        description = "Payload de soumission d'une évaluation à un autre utilisateur "
                + "(ex. acheteur évaluant un vendeur après une transaction)."
)
public class RatingRequest {

    @NotNull(message = "La note est requise")
    @Min(value = 1, message = "La note minimale est 1")
    @Max(value = 5, message = "La note maximale est 5")
    @Schema(description = "Note attribuée, comprise entre 1 (très mauvais) et 5 (excellent).",
            example = "5",
            minimum = "1", maximum = "5",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer note;

    @Size(max = 2000, message = "Le commentaire ne doit pas dépasser 2000 caractères")
    @Schema(description = "Commentaire libre justifiant la note (optionnel).",
            example = "Transaction fluide, matériel conforme à la description. Je recommande.",
            maxLength = 2000,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String commentaire;

    @Schema(description = "Identifiant de l'annonce ayant motivé l'évaluation. "
            + "Permet d'éviter les doublons par couple (évaluateur, évalué, annonce).",
            example = "1024",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long annonceId;
}
