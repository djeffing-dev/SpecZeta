package com.djeffing.SpecZeta.domain.user.controller;

import com.djeffing.SpecZeta.domain.user.dto.AuthResponse;
import com.djeffing.SpecZeta.domain.user.dto.LoginRequest;
import com.djeffing.SpecZeta.domain.user.dto.Token;
import com.djeffing.SpecZeta.domain.user.dto.UserRegistrationRequest;
import com.djeffing.SpecZeta.domain.user.service.AuthService;
import com.djeffing.SpecZeta.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification",
        description = "Endpoints publics permettant à un client d'obtenir un JWT, "
                + "soit par création de compte, soit par connexion locale.")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Créer un compte utilisateur et obtenir un JWT",
            description = """
                    Inscrit un nouvel utilisateur à partir de son email, mot de passe et pseudo
                    (la ville est optionnelle). Le mot de passe est hashé en BCrypt côté serveur.

                    En retour, l'API génère immédiatement un `accessToken` JWT exploitable : il n'est
                    **pas** nécessaire d'appeler `/api/auth/login` après l'inscription.

                    **Cas d'erreur fréquents :**
                    - `400` : payload invalide (email malformé, mot de passe trop court…).
                    - `409` : email déjà associé à un compte existant.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Compte créé. Le JWT est retourné dans le champ `data.accessToken`.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Inscription réussie",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Inscription réussie",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 86400,
                                                "user": {
                                                  "id": 42,
                                                  "email": "jane.doe@example.com",
                                                  "pseudo": "JaneD",
                                                  "ville": "Lyon",
                                                  "ratingMoyenne": 0.0,
                                                  "nombreEvaluations": 0
                                                }
                                              },
                                              "timestamp": "2026-06-03T10:15:30"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Données d'inscription invalides (validation Bean Validation).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email déjà utilisé par un autre compte.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        return authService.register(request);
    }

    @Operation(
            summary = "S'authentifier en local (email + mot de passe) et obtenir un JWT",
            description = """
                    Authentifie un utilisateur via son couple email / mot de passe. La vérification
                    est déléguée à `AuthenticationManager` (Spring Security + BCrypt).

                    Le JWT retourné doit être fourni dans l'en-tête `Authorization: Bearer <token>`
                    pour appeler les endpoints sécurisés.

                    **Cas d'erreur fréquents :**
                    - `400` : payload mal formé.
                    - `401` : identifiants invalides ou compte inexistant.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authentification réussie. Le JWT est retourné dans `data.accessToken`.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Connexion réussie",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Connexion réussie",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 86400,
                                                "user": {
                                                  "id": 42,
                                                  "email": "jane.doe@example.com",
                                                  "pseudo": "JaneD",
                                                  "ville": "Lyon",
                                                  "ratingMoyenne": 4.6,
                                                  "nombreEvaluations": 18
                                                }
                                              },
                                              "timestamp": "2026-06-03T10:18:42"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide (email ou mot de passe manquant).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Identifiants incorrects ou compte inexistant.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        // 1. On appelle le service UNE SEULE fois
        ResponseEntity<?> authResponse = authService.login(request);

        // 2. Si le service dit que c'est non autorisé (courriel non validé par exemple)
        if (authResponse.getStatusCode() == HttpStatus.FORBIDDEN) {
            // On intercepte et on renvoie un statut 403 FORBIDDEN à la place
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Veuillez vérifier votre adresse email avant de vous connecter."));
        }

        // 3. On réutilise le corps (body) de la réponse déjà obtenue pour éviter le double appel
        return ResponseEntity.ok(ApiResponse.ok(authResponse.getBody(), "Connexion réussie"));
    }

    @Operation(
            summary = "Renouveler l'accessToken via un Refresh Token",
            description = """
                    Prend un Refresh Token en paramètre de chemin et génère une nouvelle paire
                    de jetons (Access Token + Refresh Token).

                    Le Refresh Token fourni est invalidé (supprimé de la BDD) et remplacé par le nouveau.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Nouveau jeton généré.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh Token invalide, expiré ou révoqué.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/refreshToken/{refreshToken}")
    public ResponseEntity<ApiResponse<Token>> getNewAccesstokenByRefreshToken(
            @Valid @PathVariable String refreshToken){
        Token token = authService.getNewAssesstokenByRefreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.ok(token, "génération de token réussie"));
    }

}
