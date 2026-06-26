package com.djeffing.SpecZeta.domain.annonce.dto;

import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import com.djeffing.SpecZeta.domain.annonce.enums.EtatEsthetique;
import com.djeffing.SpecZeta.domain.annonce.enums.ModeRemise;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
        name = "CreateAnnonceRequest",
        description = "Payload de création d'une annonce. L'annonce est initialisée en statut "
                + "`EN_ATTENTE` et devra être publiée explicitement après upload des photos."
)
public class CreateAnnonceRequest {

    @NotBlank(message = "Le titre est requis")
    @Size(min = 5, max = 200)
    @Schema(description = "Titre court et descriptif de l'annonce.",
            example = "PC Portable Dell XPS 15 (2023) — Core i7, 32 Go RAM, RTX 4060",
            minLength = 5, maxLength = 200,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String titre;

    @NotBlank(message = "La description est requise")
    @Size(min = 20, max = 10000)
    @Schema(description = "Description détaillée du produit (état réel, accessoires inclus, historique d'utilisation…).",
            example = "Vendu avec chargeur d'origine et sacoche. Utilisé pour du dev quotidien pendant 18 mois, aucun choc.",
            minLength = 20, maxLength = 10000,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull(message = "Le prix est requis")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Prix demandé en euros. Doit être strictement positif.",
            example = "1499.00",
            minimum = "0.01",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal prix;

    @NotNull(message = "La catégorie est requise")
    @Schema(description = "Catégorie principale du produit.",
            example = "ORDINATEUR_PORTABLE",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private CategorieAnnonce categorie;

    @NotNull(message = "L'état esthétique est requis")
    @Schema(description = "État général du produit.",
            example = "TRES_BON",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private EtatEsthetique etat;

    @NotNull(message = "Le mode de remise est requis")
    @Schema(description = "Mode de remise proposé à l'acheteur.",
            example = "LES_DEUX",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private ModeRemise modeRemise;

    @Schema(description = "Latitude WGS84 du point de remise (optionnel).",
            example = "45.7640",
            minimum = "-90", maximum = "90")
    private Double latitude;

    @Schema(description = "Longitude WGS84 du point de remise (optionnel).",
            example = "4.8357",
            minimum = "-180", maximum = "180")
    private Double longitude;

    @Valid
    @Schema(description = "Fiche technique détaillée du produit (optionnelle à la création).")
    private FicheTechniqueRequest ficheTechnique;
}
