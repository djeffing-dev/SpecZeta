package com.djeffing.SpecZeta.domain.user.controller;

import com.djeffing.SpecZeta.domain.annonce.dto.AnnonceListResponse;
import com.djeffing.SpecZeta.domain.annonce.enums.StatutAnnonce;
import com.djeffing.SpecZeta.domain.annonce.service.AnnonceService;
import com.djeffing.SpecZeta.domain.user.dto.DashboardResponse;
import com.djeffing.SpecZeta.domain.user.dto.RatingRequest;
import com.djeffing.SpecZeta.domain.user.dto.RatingResponse;
import com.djeffing.SpecZeta.domain.user.dto.UpdateProfileRequest;
import com.djeffing.SpecZeta.domain.user.dto.UserProfileResponse;
import com.djeffing.SpecZeta.domain.user.service.UserService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs",
        description = "Gestion du profil de l'utilisateur connecté, consultation du profil public "
                + "d'un autre utilisateur, tableau de bord vendeur et système de notation.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AnnonceService annonceService;

    @Operation(
            summary = "Obtenir le profil complet de l'utilisateur connecté",
            description = """
                    Retourne le profil privé complet de l'utilisateur authentifié (email, ville,
                    téléphone, biographie, adresse, statistiques de notation, etc.).

                    L'identifiant est extrait du JWT — il n'est pas possible de lire le profil d'un
                    autre utilisateur via cet endpoint (utiliser `GET /api/users/{id}` à la place).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil renvoyé."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré.")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyProfile(userId)));
    }

    @Operation(
            summary = "Mettre à jour le profil de l'utilisateur connecté",
            description = """
                    Met à jour le profil de l'utilisateur connecté (PATCH partiel applicatif :
                    seuls les champs non `null` du payload sont modifiés).

                    Pour réinitialiser un champ optionnel à `null`, transmettre explicitement
                    la valeur `null` dans le JSON.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil mis à jour."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payload invalide."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré.")
    })
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(userService.updateMyProfile(userId, request), "Profil mis à jour"));
    }

    @Operation(
            summary = "Tableau de bord du vendeur connecté",
            description = """
                    Retourne les compteurs et métriques agrégés du vendeur connecté :
                    - nombre d'annonces par statut (active, vendue, en attente, suspendue),
                    - revenu total cumulé sur les annonces vendues,
                    - conversations contenant des messages non lus,
                    - nombre de fois où ses annonces ont été mises en favori,
                    - moyenne et nombre d'évaluations reçues.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tableau de bord renvoyé."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré.")
    })
    @GetMapping("/me/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.getDashboard(userId)));
    }

    @Operation(
            summary = "Lister les annonces du vendeur connecté",
            description = """
                    Liste paginée des annonces appartenant au vendeur connecté, tous statuts
                    confondus par défaut. Le paramètre `statut` permet de filtrer (ex. uniquement
                    les `ACTIVE` ou les `VENDUE`).

                    Différent de `GET /api/annonces` (public) qui ne renvoie que les annonces
                    `ACTIVE` et masque les annonces en brouillon (EN_ATTENTE) ou suspendues.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page d'annonces du vendeur."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré.")
    })
    @GetMapping("/me/annonces")
    public ResponseEntity<ApiResponse<PagedResponse<AnnonceListResponse>>> mesAnnonces(
            @Parameter(description = "Filtre optionnel par statut.", example = "ACTIVE")
            @RequestParam(required = false) StatutAnnonce statut,
            @Parameter(hidden = true)
            @PageableDefault(size = 20) Pageable pageable) {
        Long vendeurId = SecurityUtils.getCurrentUserId();
        Page<AnnonceListResponse> page = annonceService.findByVendeur(vendeurId, statut, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(page)));
    }

    @Operation(
            summary = "Consulter le profil public d'un autre utilisateur",
            description = """
                    Retourne le profil public d'un autre utilisateur (typiquement un vendeur dont
                    on consulte la fiche depuis une annonce). Les champs sensibles (email,
                    téléphone, adresse précise) sont masqués selon les règles métier du service.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil public renvoyé."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur introuvable.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getPublicProfile(
            @Parameter(description = "Identifiant de l'utilisateur à consulter.", example = "42")
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getPublicProfile(id)));
    }

    @Operation(
            summary = "Évaluer un autre utilisateur (note + commentaire)",
            description = """
                    Soumet une évaluation à un utilisateur tiers (acheteur → vendeur ou inverse)
                    à l'issue d'une transaction. La note doit être comprise entre **1 et 5**.

                    Règles métier appliquées par le service :
                    - **Auto-évaluation interdite** (un utilisateur ne peut pas se noter lui-même).
                    - **Pas de doublon** pour un même couple (évaluateur, évalué, annonce).
                    - Le champ `annonceId` est optionnel mais recommandé pour contextualiser la note.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Évaluation enregistrée."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Note hors bornes, auto-évaluation tentée ou évaluation déjà existante pour cette annonce."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur évalué introuvable.")
    })
    @PostMapping("/{id}/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> submitRating(
            @Parameter(description = "Identifiant de l'utilisateur à évaluer.", example = "42")
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request) {
        Long evaluateurId = SecurityUtils.getCurrentUserId();
        RatingResponse rating = userService.submitRating(evaluateurId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(rating, "Évaluation enregistrée"));
    }
}
