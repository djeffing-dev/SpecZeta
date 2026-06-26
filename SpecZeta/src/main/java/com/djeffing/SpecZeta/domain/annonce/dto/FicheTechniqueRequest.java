package com.djeffing.SpecZeta.domain.annonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
        name = "FicheTechniqueRequest",
        description = "Caractéristiques techniques d'un produit. Tous les champs sont optionnels — "
                + "fournir ceux qui sont pertinents pour la catégorie (ex. `gpu` n'a pas de sens "
                + "pour un smartphone)."
)
public class FicheTechniqueRequest {

    @Size(max = 150)
    @Schema(description = "Modèle exact du produit.", example = "XPS 15 9530", maxLength = 150)
    private String modele;

    @Size(max = 100)
    @Schema(description = "Marque / constructeur.", example = "Dell", maxLength = 100)
    private String marque;

    @Size(max = 200)
    @Schema(description = "Référence du processeur.", example = "Intel Core i7-13700H", maxLength = 200)
    private String processeur;

    @Size(max = 200)
    @Schema(description = "Référence du GPU (le cas échéant).", example = "NVIDIA RTX 4060 8 Go", maxLength = 200)
    private String gpu;

    @Schema(description = "Quantité de RAM en gigaoctets.", example = "32", minimum = "0")
    private Integer ramGo;

    @Schema(description = "Capacité de stockage interne en gigaoctets.", example = "1024", minimum = "0")
    private Integer stockageGo;

    @Size(max = 30)
    @Schema(description = "Type de stockage.", example = "NVMe", maxLength = 30,
            allowableValues = {"HDD", "SSD", "NVMe", "eMMC"})
    private String typeStockage;

    @Size(max = 30)
    @Schema(description = "Taille de l'écran (avec unité).", example = "15.6\"", maxLength = 30)
    private String ecranTaille;

    @Size(max = 30)
    @Schema(description = "Résolution native de l'écran.", example = "3456x2160", maxLength = 30)
    private String ecranResolution;

    @Size(max = 50)
    @Schema(description = "Socket CPU (pertinent pour les composants PC).", example = "LGA1700", maxLength = 50)
    private String socket;

    @Size(max = 30)
    @Schema(description = "Source de provenance des données techniques (alimentation auto vs saisie utilisateur).",
            example = "TECHPOWERUP", maxLength = 30)
    private String sourceApi;

    @Schema(description = "Données brutes JSON renvoyées par l'API tierce (pour audit / debug).",
            example = "{\"cores\":16,\"baseFreqMhz\":2400}")
    private String rawDataJson;
}
