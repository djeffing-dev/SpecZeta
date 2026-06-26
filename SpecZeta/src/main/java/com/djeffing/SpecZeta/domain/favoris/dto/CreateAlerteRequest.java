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
        name = "CreateAlerteRequest",
        description = "Payload de création d'une alerte de recherche. Tous les critères sont "
                + "optionnels et combinables ; l'alerte est créée active par défaut."
)
public class CreateAlerteRequest {

    @Size(max = 255)
    @Schema(description = "Mots-clés à rechercher dans le titre et la description (séparés par des espaces).",
            example = "rtx 4060 xps",
            maxLength = 255)
    private String motsCles;

    @DecimalMin(value = "0.00")
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Prix minimum recherché (EUR, inclus).", example = "500.00", minimum = "0.00")
    private BigDecimal prixMin;

    @DecimalMin(value = "0.00")
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Prix maximum recherché (EUR, inclus).", example = "2000.00", minimum = "0.00")
    private BigDecimal prixMax;

    @Schema(description = "Catégorie ciblée par l'alerte (optionnel — toutes catégories si absent).",
            example = "ORDINATEUR_PORTABLE")
    private CategorieAnnonce categorie;

    @Size(max = 120)
    @Schema(description = "Ville ou code postal de référence pour la recherche géographique.",
            example = "Lyon", maxLength = 120)
    private String localisation;

    @Min(value = 1, message = "Le rayon doit être supérieur ou égal à 1 km")
    @Max(value = 500, message = "Le rayon ne peut excéder 500 km")
    @Schema(description = "Rayon de recherche autour de la localisation, en kilomètres (1 à 500).",
            example = "50", minimum = "1", maximum = "500")
    private Integer rayonKm;

    @Builder.Default
    @Schema(description = "État initial de l'alerte. `true` par défaut.",
            example = "true",
            defaultValue = "true")
    private Boolean active = true;
}
