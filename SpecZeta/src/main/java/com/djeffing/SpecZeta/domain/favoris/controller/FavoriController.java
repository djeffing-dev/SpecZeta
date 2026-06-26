package com.djeffing.SpecZeta.domain.favoris.controller;

import com.djeffing.SpecZeta.domain.favoris.dto.AddFavoriRequest;
import com.djeffing.SpecZeta.domain.favoris.dto.FavoriResponse;
import com.djeffing.SpecZeta.domain.favoris.service.FavoriService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favoris")
@RequiredArgsConstructor
@Tag(name = "Favoris",
        description = "Gestion des annonces favorites de l'utilisateur connecté. "
                + "Toutes les opérations sont scoped à l'utilisateur extrait du JWT.")
@SecurityRequirement(name = "bearerAuth")
public class FavoriController {

    private final FavoriService favoriService;

    @Operation(
            summary = "Lister les favoris de l'utilisateur connecté",
            description = """
                    Retourne une page paginée des annonces ajoutées en favori par l'utilisateur
                    courant. Tri par défaut sur `createdAt` décroissant (les favoris les plus
                    récents en tête).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page de favoris."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<FavoriResponse>>> list(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<FavoriResponse> page = favoriService.list(userId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(page)));
    }

    @Operation(
            summary = "Ajouter une annonce aux favoris",
            description = """
                    Ajoute une annonce aux favoris de l'utilisateur connecté.

                    **Idempotent :** un appel répété sur la même annonce ne crée pas de doublon
                    et renvoie le favori existant — utile pour gérer les états désynchronisés
                    côté frontend (clic répété sur le cœur).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Annonce ajoutée (ou déjà présente) aux favoris."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payload invalide."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce introuvable.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriResponse>> add(
            @Valid @RequestBody AddFavoriRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        FavoriResponse favori = favoriService.add(userId, request.getAnnonceId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(favori, "Annonce ajoutée aux favoris"));
    }

    @Operation(
            summary = "Retirer une annonce des favoris",
            description = """
                    Retire l'annonce des favoris de l'utilisateur courant.

                    **Idempotent :** renvoie `200` même si l'annonce n'était pas favoris-ée —
                    cohérent avec un état UI désynchronisé.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonce retirée (ou déjà absente) des favoris."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré.")
    })
    @DeleteMapping("/{annonceId}")
    public ResponseEntity<ApiResponse<Void>> remove(
            @Parameter(description = "Identifiant de l'annonce à retirer des favoris.", example = "1024")
            @PathVariable Long annonceId) {
        Long userId = SecurityUtils.getCurrentUserId();
        favoriService.remove(userId, annonceId);
        return ResponseEntity.ok(ApiResponse.message("Annonce retirée des favoris"));
    }

    @Operation(
            summary = "Vérifier si une annonce est en favoris",
            description = """
                    Retourne un booléen indiquant si l'annonce est actuellement dans les favoris
                    de l'utilisateur courant. Endpoint léger utilisé par le frontend pour afficher
                    l'état du cœur sur la page de détail d'une annonce.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booléen `true`/`false`."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré.")
    })
    @GetMapping("/{annonceId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> exists(
            @Parameter(description = "Identifiant de l'annonce à tester.", example = "1024")
            @PathVariable Long annonceId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(favoriService.isFavorite(userId, annonceId)));
    }
}
