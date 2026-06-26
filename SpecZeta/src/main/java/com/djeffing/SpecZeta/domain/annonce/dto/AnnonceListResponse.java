package com.djeffing.SpecZeta.domain.annonce.dto;

import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import com.djeffing.SpecZeta.domain.annonce.enums.EtatEsthetique;
import com.djeffing.SpecZeta.domain.annonce.enums.StatutAnnonce;
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
        name = "AnnonceListResponse",
        description = "Vue résumée d'une annonce, optimisée pour l'affichage en grille / liste "
                + "(catalogue public, favoris, dashboard vendeur)."
)
public class AnnonceListResponse {

    @Schema(description = "Identifiant unique de l'annonce.", example = "1024")
    private Long id;

    @Schema(description = "Titre de l'annonce.",
            example = "PC Portable Dell XPS 15 (2023) — Core i7, 32 Go RAM, RTX 4060")
    private String titre;

    @Schema(description = "Prix demandé en euros.", example = "1499.00")
    private BigDecimal prix;

    @Schema(description = "Catégorie principale.", example = "ORDINATEUR_PORTABLE")
    private CategorieAnnonce categorie;

    @Schema(description = "État esthétique du produit.", example = "TRES_BON")
    private EtatEsthetique etat;

    @Schema(description = "Statut courant de l'annonce.", example = "ACTIVE")
    private StatutAnnonce statut;

    @Schema(description = "`true` si une certification benchmark vérifiée est attachée à l'annonce.",
            example = "true")
    private Boolean certifiee;

    @Schema(description = "URL de la photo principale de l'annonce.",
            example = "https://dl.dropboxusercontent.com/s/abc/annonce-1024-1.jpg",
            format = "uri")
    private String photoPrincipaleUrl;

    @Schema(description = "Pseudo du vendeur (affichage rapide).", example = "JaneD")
    private String vendeurPseudo;

    @Schema(description = "Ville du vendeur (affichage rapide).", example = "Lyon")
    private String vendeurVille;

    @Schema(description = "Date de création de l'annonce (ISO-8601).",
            example = "2026-05-21T14:03:22")
    private LocalDateTime createdAt;
}
