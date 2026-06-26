package com.djeffing.SpecZeta.domain.annonce.controller;

import com.djeffing.SpecZeta.domain.annonce.dto.AnnonceListResponse;
import com.djeffing.SpecZeta.domain.annonce.dto.AnnonceResponse;
import com.djeffing.SpecZeta.domain.annonce.dto.CertificationRequest;
import com.djeffing.SpecZeta.domain.annonce.dto.CreateAnnonceRequest;
import com.djeffing.SpecZeta.domain.annonce.dto.UpdateAnnonceRequest;
import com.djeffing.SpecZeta.domain.annonce.dto.UpdateStatutRequest;
import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import com.djeffing.SpecZeta.domain.annonce.enums.EtatEsthetique;
import com.djeffing.SpecZeta.domain.annonce.service.AnnonceService;
import com.djeffing.SpecZeta.security.SecurityUtils;
import com.djeffing.SpecZeta.shared.dto.ApiResponse;
import com.djeffing.SpecZeta.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
@Tag(name = "Annonces",
        description = "Catalogue des annonces : consultation publique, création et gestion par le vendeur, "
                + "certification benchmark.")
public class AnnonceController {

    private final AnnonceService annonceService;

    @Operation(
            summary = "Lister les annonces actives (endpoint public)",
            description = """
                    Retourne une page d'annonces actives, filtrable par catégorie, état, fourchette
                    de prix, et présence d'une certification benchmark vérifiée.

                    Tous les filtres sont optionnels et combinables. La pagination suit la convention
                    Spring Data : `page`, `size`, `sort` (ex. `?sort=createdAt,desc`).

                    Endpoint **public** : aucune authentification requise.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Page d'annonces correspondant aux filtres.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AnnonceListResponse>>> list(
            @Parameter(description = "Catégorie d'annonce (filtre optionnel).", example = "ORDINATEUR_PORTABLE")
            @RequestParam(required = false) CategorieAnnonce categorie,
            @Parameter(description = "État esthétique du produit (filtre optionnel).", example = "TRES_BON")
            @RequestParam(required = false) EtatEsthetique etat,
            @Parameter(description = "Prix minimum (EUR, inclus).", example = "150.00")
            @RequestParam(required = false) BigDecimal prixMin,
            @Parameter(description = "Prix maximum (EUR, inclus).", example = "1200.00")
            @RequestParam(required = false) BigDecimal prixMax,
            @Parameter(description = "Si `true`, ne retourne que les annonces avec une certification benchmark vérifiée.",
                    example = "true")
            @RequestParam(required = false) Boolean certifieeOnly,
            @Parameter(hidden = true)
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AnnonceListResponse> page = annonceService.list(
                categorie, etat, prixMin, prixMax, certifieeOnly, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(page)));
    }

    @Operation(
            summary = "Obtenir le détail complet d'une annonce (endpoint public)",
            description = """
                    Retourne la vue complète d'une annonce : caractéristiques, vendeur, fiche
                    technique, certification benchmark et galerie photos.

                    Endpoint **public** : aucune authentification requise.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Détail complet de l'annonce.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Aucune annonce ne correspond à l'identifiant fourni.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnonceResponse>> findById(
            @Parameter(description = "Identifiant unique de l'annonce.", example = "1024", in = ParameterIn.PATH)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(annonceService.findById(id)));
    }

    @Operation(
            summary = "Créer une nouvelle annonce",
            description = """
                    Crée une annonce pour le vendeur identifié par le JWT. L'annonce est créée en
                    statut `EN_ATTENTE` et reste invisible publiquement tant que :
                    1. 3 à 5 photos ont été uploadées via `POST /api/annonces/{id}/medias`.
                    2. Le statut a été basculé sur `ACTIVE` via `PATCH /api/annonces/{id}/statut`.

                    La fiche technique est optionnelle au moment de la création et peut être ajoutée
                    ou complétée plus tard via `PUT /api/annonces/{id}`.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Annonce créée. Le statut initial est `EN_ATTENTE`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide (champs obligatoires manquants ou contraintes non respectées)."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT manquant ou expiré.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<AnnonceResponse>> create(
            @Valid @RequestBody CreateAnnonceRequest request) {
        Long vendeurId = SecurityUtils.getCurrentUserId();
        AnnonceResponse created = annonceService.create(vendeurId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Annonce créée"));
    }

    @Operation(
            summary = "Mettre à jour une annonce existante",
            description = """
                    Met à jour une annonce existante (PATCH partiel applicatif : seuls les champs
                    fournis sont modifiés). Seul le propriétaire de l'annonce peut la modifier ;
                    toute tentative par un autre utilisateur authentifié renvoie `403`.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonce mise à jour."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payload invalide."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "L'utilisateur n'est pas propriétaire de l'annonce."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce introuvable.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnonceResponse>> update(
            @Parameter(description = "Identifiant de l'annonce à modifier.", example = "1024")
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnnonceRequest request) {
        Long vendeurId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(annonceService.update(vendeurId, id, request), "Annonce mise à jour"));
    }

    @Operation(
            summary = "Supprimer une annonce",
            description = """
                    Supprime définitivement l'annonce et toutes ses dépendances (médias Dropbox,
                    fiche technique, certification, conversations associées). Action irréversible
                    réservée au propriétaire.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonce supprimée."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "L'utilisateur n'est pas propriétaire de l'annonce."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce introuvable.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Identifiant de l'annonce à supprimer.", example = "1024")
            @PathVariable Long id) {
        Long vendeurId = SecurityUtils.getCurrentUserId();
        annonceService.delete(vendeurId, id);
        return ResponseEntity.ok(ApiResponse.message("Annonce supprimée"));
    }

    @Operation(
            summary = "Changer le statut d'une annonce",
            description = """
                    Permet au propriétaire de faire évoluer le statut de son annonce :
                    - `EN_ATTENTE` → `ACTIVE` : publie l'annonce. **Requiert 3 à 5 photos uploadées.**
                    - `ACTIVE` → `SUSPENDUE` : retire temporairement de la recherche.
                    - `ACTIVE` → `VENDUE` : marque comme vendue.
                    - `SUSPENDUE` → `ACTIVE` : remet en ligne.

                    Toute transition invalide est rejetée en `400`.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statut mis à jour."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Transition de statut invalide (ex. passage en ACTIVE sans 3-5 photos)."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "L'utilisateur n'est pas propriétaire de l'annonce."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce introuvable.")
    })
    @PatchMapping("/{id}/statut")
    public ResponseEntity<ApiResponse<AnnonceResponse>> updateStatut(
            @Parameter(description = "Identifiant de l'annonce.", example = "1024")
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatutRequest request) {
        Long vendeurId = SecurityUtils.getCurrentUserId();
        AnnonceResponse updated = annonceService.updateStatut(vendeurId, id, request.getStatut());
        return ResponseEntity.ok(ApiResponse.ok(updated, "Statut mis à jour"));
    }

    @Operation(
            summary = "Soumettre une certification benchmark pour une annonce",
            description = """
                    Attache un résultat de benchmark (Geekbench, 3DMark, CPU-Z…) à l'annonce afin
                    de la marquer comme « certifiée ». L'URL fournie est validée contre une liste
                    blanche de domaines pour éviter les faux résultats.

                    Réservé au propriétaire de l'annonce.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Certification enregistrée et liée à l'annonce."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Payload invalide ou domaine d'URL non autorisé."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "L'utilisateur n'est pas propriétaire de l'annonce."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce introuvable.")
    })
    @PostMapping("/{id}/certification")
    public ResponseEntity<ApiResponse<AnnonceResponse>> submitCertification(
            @Parameter(description = "Identifiant de l'annonce à certifier.", example = "1024")
            @PathVariable Long id,
            @Valid @RequestBody CertificationRequest request) {
        Long vendeurId = SecurityUtils.getCurrentUserId();
        AnnonceResponse updated = annonceService.submitCertification(vendeurId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(updated, "Certification enregistrée"));
    }
}
