package com.djeffing.SpecZeta.domain.user.controller;

import com.djeffing.SpecZeta.domain.user.dto.AuthResponse;
import com.djeffing.SpecZeta.domain.user.service.GoogleOauth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
@Tag(name = "Authentification OAuth2 (Google)",
        description = "Connexion / inscription déléguée via le fournisseur Google (OAuth2 + OpenID Connect). "
                + "Endpoints publics : aucun JWT n'est requis pour initier le flux.")
public class GoogleOauth2Controller {

    private final GoogleOauth2Service googleOauth2Service;

    @Operation(
            summary = "Générer l'URL de consentement Google (étape 1 du flux OAuth2)",
            description = """
                    Construit et retourne l'URL d'autorisation Google vers laquelle le client doit
                    rediriger l'utilisateur pour qu'il s'authentifie et accorde son consentement.

                    Les scopes demandés sont `email`, `profile` et `openid`. Une fois le consentement
                    donné, Google redirige le navigateur vers la `redirect-uri` configurée côté serveur
                    en joignant un paramètre `code` à usage unique, à transmettre ensuite à
                    `GET /api/auth/google/getToken`.

                    **Réponse :** une chaîne brute contenant l'URL complète (et non un objet JSON enveloppé).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "URL de consentement Google générée.",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(
                                    name = "URL d'autorisation",
                                    value = "https://accounts.google.com/o/oauth2/auth"
                                            + "?client_id=1234567890-abc.apps.googleusercontent.com"
                                            + "&redirect_uri=http://localhost:8080/api/auth/google/getToken"
                                            + "&response_type=code"
                                            + "&scope=email%20profile%20openid"
                            )
                    )
            )
    })
    @GetMapping("/getUrl")
    public ResponseEntity<?> createUrl() {
        String url = googleOauth2Service.createUrl();
        return ResponseEntity.ok(url);
    }

    @Operation(
            summary = "Échanger le code d'autorisation Google contre un JWT (étape 2 du flux OAuth2)",
            description = """
                    Endpoint de callback appelé après le consentement de l'utilisateur sur Google.
                    Il échange le `code` d'autorisation à usage unique contre les jetons Google,
                    vérifie l'`id_token` (signature et audience), puis :

                    - récupère l'utilisateur existant via son email, **ou** crée automatiquement un
                      nouveau compte (email, pseudo et photo de profil issus de Google, `emailVerified = true`) ;
                    - génère un couple de jetons applicatifs (`accessToken` + `refreshToken`) propre à SpecZeta.

                    Le `accessToken` retourné s'utilise ensuite comme tout JWT local, dans l'en-tête
                    `Authorization: Bearer <token>`.

                    **Cas d'erreur fréquents :**
                    - `401` : `id_token` invalide, échec de la vérification, ou échec de l'échange du code avec Google
                      (code expiré ou déjà consommé).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authentification Google réussie. Les jetons SpecZeta sont retournés.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Connexion Google réussie",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "tokenType": "Bearer",
                                              "expiresIn": 86400,
                                              "user": {
                                                "id": 57,
                                                "email": "jane.doe@gmail.com",
                                                "pseudo": "Jane Doe",
                                                "photoUrl": "https://lh3.googleusercontent.com/a/default-user",
                                                "ville": null
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Code invalide/expiré, échec de l'échange avec Google ou vérification de l'id_token échouée.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Token invalide",
                                    value = """
                                            {
                                              "error": "Invalid ID token"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/getToken")
    public ResponseEntity<?> callBack(
            @Parameter(description = "Code d'autorisation à usage unique renvoyé par Google sur la redirect-uri.",
                    required = true,
                    example = "4/0AeaYSHCxYz...")
            @RequestParam String code) {
        return googleOauth2Service.callBack(code);
    }

}
