package com.djeffing.SpecZeta.domain.favoris.dto;

import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "UpdateAlerteRequest",
        description = "Payload de mise à jour partielle d'une alerte de recherche. "
                + "Seuls les champs non `null` du payload sont modifiés. "
                + "Permet notamment d'activer/désactiver une alerte via le champ `active`."
)
public class UpdateAlerteRequest {

    @Size(max = 255)
    @Schema(description = "Nouveaux mots-clés (espace-séparés).",
            example = "macbook pro m3", maxLength = 255,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String motsCles;

    @DecimalMin(value = "0.00")
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Nouveau prix minimum.", example = "1000.00",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal prixMin;

    @DecimalMin(value = "0.00")
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Nouveau prix maximum.", example = "2500.00",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal prixMax;

    @Schema(description = "Nouvelle catégorie.", example = "ORDINATEUR_PORTABLE",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private CategorieAnnonce categorie;

    @Size(max = 120)
    @Schema(description = "Nouvelle localisation.", example = "Paris", maxLength = 120,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String localisation;

    @Min(1)
    @Max(500)
    @Schema(description = "Nouveau rayon en km (1 à 500).", example = "100",
            minimum = "1", maximum = "500",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer rayonKm;

    @Schema(description = "Active (`true`) ou désactive (`false`) l'alerte.",
            example = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;
}
