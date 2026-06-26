package com.djeffing.SpecZeta.domain.favoris.controller;

import com.djeffing.SpecZeta.domain.favoris.dto.AlerteResponse;
import com.djeffing.SpecZeta.domain.favoris.dto.CreateAlerteRequest;
import com.djeffing.SpecZeta.domain.favoris.dto.UpdateAlerteRequest;
import com.djeffing.SpecZeta.domain.favoris.service.AlerteRechercheService;
import com.djeffing.SpecZeta.security.SecurityUtils;
import com.djeffing.SpecZeta.shared.dto.ApiResponse;
import com.djeffing.SpecZeta.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
@Tag(name = "Alertes de recherche",
        description = "Alertes paramétrables (mots-clés, catégorie, fourchette de prix, localisation) "
                + "qui notifient l'utilisateur lorsqu'une annonce correspond à ses critères.")
@SecurityRequirement(name = "bearerAuth")
public class AlerteRechercheController {

    private final AlerteRechercheService alerteService;

    @Operation(
            summary = "Lister les alertes de l'utilisateur connecté",
            description = """
                    Retourne une page paginée des alertes de recherche définies par l'utilisateur
                    courant. Tri par défaut sur `createdAt` décroissant.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page d'alertes."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AlerteResponse>>> list(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<AlerteResponse> page = alerteService.list(userId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(page)));
    }

    @Operation(
            summary = "Créer une nouvelle alerte de recherche",
            description = """
                    Crée une alerte de recherche pour l'utilisateur courant. Tous les critères
                    sont optionnels et combinables : mots-clés, fourchette de prix, catégorie,
                    localisation, rayon de recherche (1 à 500 km).

                    L'alerte est créée **active par défaut** ; le worker de notification l'évaluera
                    à chaque nouvelle annonce publiée correspondant aux critères.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Alerte créée."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payload invalide."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<AlerteResponse>> create(
            @Valid @RequestBody CreateAlerteRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        AlerteResponse created = alerteService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Alerte créée"));
    }

    @Operation(
            summary = "Mettre à jour une alerte de recherche",
            description = """
                    Met à jour les critères d'une alerte existante (PATCH partiel applicatif).
                    Permet notamment d'**activer/désactiver** une alerte via le champ `active`
                    sans avoir à la supprimer puis la recréer.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerte mise à jour."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payload invalide."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "L'alerte n'appartient pas à l'utilisateur courant."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerte introuvable.")
    })
    @PutMapping("/{alerteId}")
    public ResponseEntity<ApiResponse<AlerteResponse>> update(
            @Parameter(description = "Identifiant de l'alerte à modifier.", example = "12")
            @PathVariable Long alerteId,
            @Valid @RequestBody UpdateAlerteRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        AlerteResponse updated = alerteService.update(userId, alerteId, request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Alerte mise à jour"));
    }

    @Operation(
            summary = "Supprimer une alerte de recherche",
            description = """
                    Supprime définitivement l'alerte. Pour simplement **désactiver** sans perdre
                    la configuration, préférer `PUT /api/alertes/{alerteId}` avec
                    `{ "active": false }`.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerte supprimée."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "L'alerte n'appartient pas à l'utilisateur courant."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerte introuvable.")
    })
    @DeleteMapping("/{alerteId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Identifiant de l'alerte à supprimer.", example = "12")
            @PathVariable Long alerteId) {
        Long userId = SecurityUtils.getCurrentUserId();
        alerteService.delete(userId, alerteId);
        return ResponseEntity.ok(ApiResponse.message("Alerte supprimée"));
    }
}
