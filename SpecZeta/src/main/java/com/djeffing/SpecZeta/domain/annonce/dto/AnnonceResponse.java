package com.djeffing.SpecZeta.domain.annonce.dto;

import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import com.djeffing.SpecZeta.domain.annonce.enums.EtatEsthetique;
import com.djeffing.SpecZeta.domain.annonce.enums.ModeRemise;
import com.djeffing.SpecZeta.domain.annonce.enums.StatutAnnonce;
import com.djeffing.SpecZeta.domain.user.dto.UserSummaryResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "AnnonceResponse",
        description = "Vue lecture complète d'une annonce : caractéristiques, vendeur, fiche "
                + "technique, certification benchmark et galerie photos. Utilisée par "
                + "`GET /api/annonces/{id}`."
)
public class AnnonceResponse {

    @Schema(description = "Identifiant unique de l'annonce.", example = "1024")
    private Long id;

    @Schema(description = "Titre.",
            example = "PC Portable Dell XPS 15 (2023) — Core i7, 32 Go RAM, RTX 4060")
    private String titre;

    @Schema(description = "Description détaillée.",
            example = "Vendu avec chargeur d'origine et sacoche. Aucun choc, utilisé 18 mois.")
    private String description;

    @Schema(description = "Prix demandé en euros.", example = "1499.00")
    private BigDecimal prix;

    @Schema(description = "Catégorie principale.", example = "ORDINATEUR_PORTABLE")
    private CategorieAnnonce categorie;

    @Schema(description = "État esthétique du produit.", example = "TRES_BON")
    private EtatEsthetique etat;

    @Schema(description = "Mode de remise proposé.", example = "LES_DEUX")
    private ModeRemise modeRemise;

    @Schema(description = "Statut courant de l'annonce.", example = "ACTIVE")
    private StatutAnnonce statut;

    @Schema(description = "`true` si une certification benchmark a été validée.", example = "true")
    private Boolean certifiee;

    @Schema(description = "Latitude WGS84 du point de remise.", example = "45.7640")
    private Double latitude;

    @Schema(description = "Longitude WGS84 du point de remise.", example = "4.8357")
    private Double longitude;

    @Schema(description = "Profil résumé du vendeur.")
    private UserSummaryResponse vendeur;

    @Schema(description = "Fiche technique détaillée (peut être `null` si non renseignée).")
    private FicheTechniqueResponse ficheTechnique;

    @Schema(description = "Certification benchmark (peut être `null` si l'annonce n'est pas certifiée).")
    private CertificationResponse certification;

    @ArraySchema(
            arraySchema = @Schema(description = "Galerie de photos de l'annonce, triée par ordre d'affichage. "
                    + "3 à 5 photos pour une annonce publiée."),
            schema = @Schema(implementation = AnnonceMediaResponse.class)
    )
    @Builder.Default
    private List<AnnonceMediaResponse> medias = List.of();

    @Schema(description = "Horodatage de création de l'annonce.", example = "2026-05-21T14:03:22")
    private LocalDateTime createdAt;

    @Schema(description = "Horodatage de la dernière modification.", example = "2026-05-22T09:11:08")
    private LocalDateTime updatedAt;
}
