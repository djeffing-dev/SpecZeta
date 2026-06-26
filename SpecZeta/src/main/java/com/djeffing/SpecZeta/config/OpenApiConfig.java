package com.djeffing.SpecZeta.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration centrale de la documentation OpenAPI 3 (Swagger UI) du projet SpecZeta.
 *
 * <p>Stratégie de sécurité retenue :</p>
 * <ul>
 *   <li>Aucune exigence d'authentification globale n'est déclarée ici. L'icône de cadenas
 *       n'apparaît donc <em>que</em> sur les endpoints explicitement annotés avec
 *       {@code @SecurityRequirement(name = "bearerAuth")} (typiquement au niveau du contrôleur
 *       ou de la méthode).</li>
 *   <li>Les endpoints publics (auth, recherche, consultation d'annonces…) restent testables
 *       depuis Swagger UI sans token, conformément à la configuration
 *       {@code permitAll()} de {@code SecurityConfig}.</li>
 *   <li>Les endpoints protégés peuvent être testés via le bouton « Authorize » de Swagger UI :
 *       l'utilisateur y colle son JWT (obtenu via {@code POST /api/auth/login} ou
 *       {@code POST /api/auth/register}) au format {@code Bearer <token>}.</li>
 * </ul>
 *
 * <p>Les tags listés ici fixent l'ordre d'affichage des sections dans Swagger UI et
 * décrivent chaque domaine fonctionnel exposé par l'API.</p>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SpecZeta API — Marketplace d'accessoires informatiques",
                version = "1.0.0",
                description = """
                        API REST du projet **SpecZeta**, une marketplace dédiée à la vente
                        d'équipements et accessoires informatiques d'occasion (PC, composants,
                        périphériques, smartphones, consoles…).

                        ### Fonctionnalités exposées
                        - Authentification locale (email / mot de passe) avec JWT et OAuth2 (Google, Facebook).
                        - Gestion complète des annonces (CRUD, statut, certification benchmark, photos Dropbox).
                        - Messagerie en temps réel acheteur ↔ vendeur (REST + WebSocket STOMP).
                        - Favoris et alertes de recherche personnalisées.
                        - Profil utilisateur, tableau de bord vendeur et système de notation.

                        ### Conventions
                        - Toutes les réponses sont enveloppées dans un objet `ApiResponse<T>` standardisé
                          (succès, message, payload, erreurs, horodatage).
                        - Les listes sont paginées via `PagedResponse<T>` (paramètres `page`, `size`, `sort`).
                        - Les dates respectent le format ISO-8601 (`yyyy-MM-dd'T'HH:mm:ss`).
                        - Les montants utilisent `BigDecimal` avec deux décimales (`EUR`).

                        ### Authentification
                        Pour tester un endpoint protégé : appeler `POST /api/auth/login`, copier
                        l'`accessToken` retourné puis cliquer sur **Authorize** en haut à droite
                        et coller le token (Swagger UI ajoute automatiquement le préfixe `Bearer`).
                        """,
                contact = @Contact(
                        name = "Jefferson Tsafack",
                        email = "tsafackjefferson2001@gmail.com",
                        url = "https://djeffing.github.io/potfolio/"
                ),
                license = @License(
                        name = "Propriétaire — usage interne SpecZeta",
                        url = "https://djeffing.github.io/potfolio/"
                )
        ),
        servers = {
                @Server(description = "Environnement local (développement)", url = "http://localhost:8080"),
                @Server(description = "Environnement de production", url = "https://api.speczeta.com")
        },
        tags = {
                @Tag(name = "Authentification", description = "Inscription, connexion et délivrance du JWT."),
                @Tag(name = "Vérification OTP", description = "Vérification de l'adresse email par code à usage unique (OTP) et renvoi d'un nouveau code."),
                @Tag(name = "Utilisateurs", description = "Profil personnel, profil public, tableau de bord et système de notation."),
                @Tag(name = "Annonces", description = "Cycle de vie complet d'une annonce : création, mise à jour, statut, certification benchmark."),
                @Tag(name = "Médias", description = "Upload des photos d'une annonce vers Dropbox (3 à 5 photos par annonce)."),
                @Tag(name = "Messagerie", description = "Conversations et messages entre acheteurs et vendeurs."),
                @Tag(name = "Favoris", description = "Gestion des annonces favorites de l'utilisateur connecté."),
                @Tag(name = "Alertes de recherche", description = "Alertes paramétrables qui notifient l'utilisateur quand une annonce correspond à ses critères.")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = """
                Authentification JWT par jeton Bearer.

                **Comment l'utiliser depuis Swagger UI :**
                1. Appeler `POST /api/auth/login` (ou `POST /ap i/auth/register`).
                2. Copier la valeur du champ `accessToken` retournée dans la réponse.
                3. Cliquer sur le bouton **Authorize** en haut de cette page.
                4. Coller le token **sans** préfixe `Bearer ` (Swagger l'ajoute automatiquement).

                Le token expire au bout de la durée configurée côté serveur ; passé ce délai,
                il faut se reconnecter pour obtenir un nouveau jeton.
                """,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
