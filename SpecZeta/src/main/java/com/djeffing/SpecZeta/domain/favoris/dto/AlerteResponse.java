package com.djeffing.SpecZeta.domain.favoris.dto;

import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "AlerteResponse",
        description = "Vue lecture d'une alerte de recherche configurée par l'utilisateur."
)
public class AlerteResponse {

    @Schema(description = "Identifiant unique de l'alerte.", example = "12")
    private Long id;

    @Schema(description = "Mots-clés recherchés.", example = "rtx 4060 xps")
    private String motsCles;

    @Schema(description = "Prix minimum (EUR).", example = "500.00")
    private BigDecimal prixMin;

    @Schema(description = "Prix maximum (EUR).", example = "2000.00")
    private BigDecimal prixMax;

    @Schema(description = "Catégorie ciblée.", example = "ORDINATEUR_PORTABLE")
    private CategorieAnnonce categorie;

    @Schema(description = "Localisation de référence (ville ou code postal).", example = "Lyon")
    private String localisation;

    @Schema(description = "Rayon de recherche en km.", example = "50")
    private Integer rayonKm;

    @Schema(description = "Indique si l'alerte est actuellement active.", example = "true")
    private Boolean active;

    @Schema(description = "Date de création.", example = "2026-05-21T14:03:22")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification.", example = "2026-06-01T09:11:08")
    private LocalDateTime updatedAt;
}
