package com.djeffing.SpecZeta.domain.annonce.dto;

import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import com.djeffing.SpecZeta.domain.annonce.enums.EtatEsthetique;
import com.djeffing.SpecZeta.domain.annonce.enums.ModeRemise;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
        name = "UpdateAnnonceRequest",
        description = "Payload de mise à jour partielle d'une annonce. Tous les champs sont "
                + "optionnels : seuls les champs non `null` du payload remplacent les valeurs "
                + "existantes."
)
public class UpdateAnnonceRequest {

    @Size(min = 5, max = 200)
    @Schema(description = "Nouveau titre.", example = "PC Portable Dell XPS 15 — prix négocié",
            minLength = 5, maxLength = 200,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String titre;

    @Size(min = 20, max = 10000)
    @Schema(description = "Nouvelle description.",
            example = "Mise à jour : sacoche neuve incluse.",
            minLength = 20, maxLength = 10000,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Nouveau prix en euros.", example = "1399.00", minimum = "0.01",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal prix;

    @Schema(description = "Nouvelle catégorie.", example = "ORDINATEUR_PORTABLE",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private CategorieAnnonce categorie;

    @Schema(description = "Nouvel état esthétique.", example = "BON",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private EtatEsthetique etat;

    @Schema(description = "Nouveau mode de remise.", example = "MAIN_PROPRE",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ModeRemise modeRemise;

    @Schema(description = "Latitude WGS84.", example = "45.7640")
    private Double latitude;

    @Schema(description = "Longitude WGS84.", example = "4.8357")
    private Double longitude;

    @Valid
    @Schema(description = "Fiche technique mise à jour (remplace l'existante si fournie).")
    private FicheTechniqueRequest ficheTechnique;
}
