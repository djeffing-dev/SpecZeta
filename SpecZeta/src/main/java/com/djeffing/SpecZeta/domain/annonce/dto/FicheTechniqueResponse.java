package com.djeffing.SpecZeta.domain.annonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "FicheTechniqueResponse",
        description = "Vue lecture des caractéristiques techniques d'une annonce."
)
public class FicheTechniqueResponse {

    @Schema(description = "Identifiant interne de la fiche technique.", example = "210")
    private Long id;

    @Schema(description = "Modèle du produit.", example = "XPS 15 9530")
    private String modele;

    @Schema(description = "Marque du produit.", example = "Dell")
    private String marque;

    @Schema(description = "Processeur.", example = "Intel Core i7-13700H")
    private String processeur;

    @Schema(description = "GPU.", example = "NVIDIA RTX 4060 8 Go")
    private String gpu;

    @Schema(description = "Quantité de RAM en Go.", example = "32")
    private Integer ramGo;

    @Schema(description = "Capacité de stockage en Go.", example = "1024")
    private Integer stockageGo;

    @Schema(description = "Type de stockage.", example = "NVMe")
    private String typeStockage;

    @Schema(description = "Taille de l'écran.", example = "15.6\"")
    private String ecranTaille;

    @Schema(description = "Résolution de l'écran.", example = "3456x2160")
    private String ecranResolution;

    @Schema(description = "Socket CPU.", example = "LGA1700")
    private String socket;

    @Schema(description = "Source des données techniques.", example = "TECHPOWERUP")
    private String sourceApi;
}
