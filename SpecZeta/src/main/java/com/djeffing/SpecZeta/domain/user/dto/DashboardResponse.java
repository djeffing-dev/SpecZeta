package com.djeffing.SpecZeta.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
        name = "DashboardResponse",
        description = "Métriques agrégées du tableau de bord vendeur. "
                + "Calculées en temps réel à partir des annonces, conversations et évaluations de l'utilisateur."
)
public class DashboardResponse {

    @Schema(description = "Nombre d'annonces actuellement actives (en ligne).", example = "8")
    private long annoncesActives;

    @Schema(description = "Nombre cumulé d'annonces vendues.", example = "23")
    private long annoncesVendues;

    @Schema(description = "Nombre d'annonces en attente (brouillons non publiés).", example = "2")
    private long annoncesEnAttente;

    @Schema(description = "Nombre d'annonces suspendues (retirées temporairement).", example = "1")
    private long annoncesSuspendues;

    @Schema(description = "Revenu total cumulé en euros sur l'ensemble des ventes.",
            example = "5430.50")
    private BigDecimal revenuTotal;

    @Schema(description = "Nombre de conversations contenant au moins un message non lu.", example = "3")
    private long conversationsNonLues;

    @Schema(description = "Nombre de fois où une annonce du vendeur a été mise en favori par un autre utilisateur.",
            example = "47")
    private long favorisRecus;

    @Schema(description = "Note moyenne reçue (1.0 à 5.0).", example = "4.6")
    private Double ratingMoyenne;

    @Schema(description = "Nombre total d'évaluations reçues.", example = "18")
    private Integer nombreEvaluations;
}
