package com.djeffing.SpecZeta.domain.media.controller;

import com.djeffing.SpecZeta.domain.annonce.service.AnnonceService;
import com.djeffing.SpecZeta.security.SecurityUtils;
import com.djeffing.SpecZeta.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
@Tag(name = "Médias",
        description = "Upload des photos d'une annonce vers Dropbox. "
                + "Chaque annonce doit avoir entre 3 et 5 photos pour pouvoir passer en statut `ACTIVE`.")
@SecurityRequirement(name = "bearerAuth")
public class MediaController {

    private final AnnonceService annonceService;

    @Operation(
            summary = "Uploader (ou remplacer) les photos d'une annonce",
            description = """
                    Envoie 3 à 5 photos pour une annonce existante. L'opération est **atomique
                    par remplacement** :

                    1. Vérifie que le vendeur connecté est bien propriétaire de l'annonce.
                    2. Vérifie le nombre de fichiers reçus (`3 ≤ n ≤ 5`).
                    3. Supprime les anciennes photos de Dropbox **et** de la base.
                    4. Upload chaque nouveau fichier sur Dropbox via `DropboxStorageService`,
                       qui valide individuellement le type MIME et la taille du fichier.

                    Le format attendu est `multipart/form-data` avec un champ `files` (répété).

                    Limites par fichier (vérifiées par `StorageService`) :
                    - Types MIME autorisés : `image/jpeg`, `image/png`, `image/webp`.
                    - Taille maximale typique : 5 Mo (cf. configuration applicative).
                    """,
            requestBody = @RequestBody(
                    description = "Lot multipart contenant 3 à 5 fichiers image dans le champ `files`.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "object", requiredProperties = "files"),
                            encoding = @Encoding(name = "files", contentType = "image/jpeg, image/png, image/webp")
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Photos uploadées. La réponse contient la liste des URLs publiques Dropbox dans l'ordre d'envoi.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Nombre de fichiers hors plage 3-5, type MIME interdit, ou fichier trop volumineux."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "L'utilisateur n'est pas propriétaire de l'annonce."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce introuvable."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500",
                    description = "Erreur lors du transfert vers Dropbox (timeout, quota, indisponibilité du service tiers).")
    })
    @PostMapping(value = "/{annonceId}/medias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<String>>> uploadMedias(
            @Parameter(description = "Identifiant de l'annonce à laquelle attacher les photos.", example = "1024")
            @PathVariable Long annonceId,
            @Parameter(description = "Liste de 3 à 5 fichiers image (jpeg/png/webp).")
            @RequestParam("files") List<MultipartFile> files) {
        Long vendeurId = SecurityUtils.getCurrentUserId();
        List<String> urls = annonceService.uploadMedias(vendeurId, annonceId, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(urls, "Photos uploadées"));
    }
}
