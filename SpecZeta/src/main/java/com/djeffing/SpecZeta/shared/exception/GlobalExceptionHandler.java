package com.djeffing.SpecZeta.shared.exception;

import com.djeffing.SpecZeta.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Gère les ressources manquantes (annonce, utilisateur, conversation introuvables).
     * Renvoie 404 avec un message lisible plutôt qu'une stack trace.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Ressource introuvable : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Gère les violations de règles métier (ex. moins de 3 photos, prix négatif,
     * URL benchmark invalide). Mappé en HTTP 400.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Gère les erreurs d'authentification métier remontées par les services
     * (token absent, expiré côté logique applicative). Mappé en HTTP 401.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Intercepte les refus d'accès Spring Security (utilisateur authentifié mais
     * sans le droit demandé : ex. modifier l'annonce d'un autre vendeur). HTTP 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Accès refusé : " + ex.getMessage()));
    }

    /**
     * Gère spécifiquement l'échec de login email/password.
     * On masque la raison exacte (« utilisateur inconnu » vs. « mauvais mot de passe »)
     * pour éviter l'énumération d'emails par un attaquant.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Identifiants invalides"));
    }

    /**
     * Filet de sécurité pour toute {@link AuthenticationException} non couverte
     * par les handlers plus spécifiques au-dessus.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Erreur d'authentification : " + ex.getMessage()));
    }

    /**
     * Convertit les erreurs de validation Bean Validation ({@code @Valid}) en
     * réponse structurée listant chaque champ fautif (« email: doit être valide »).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation échouée", errors));
    }

    /**
     * Renvoie un 400 explicite quand l'upload dépasse {@code spring.servlet.multipart.max-file-size}
     * (10 Mo par fichier, 50 Mo par requête).
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Le fichier dépasse la taille maximale autorisée"));
    }

    /**
     * Validation métier d'un fichier multipart (type MIME, taille, nombre).
     * HTTP 400 avec le message exact pour que le frontend affiche l'erreur.
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFile(InvalidFileException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Échec du stockage distant (Dropbox 5xx persistant, panne réseau).
     * HTTP 503 avec un message générique : on ne fuite pas la cause technique
     * au client, et le détail reste consultable côté log serveur.
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleStorage(StorageException ex) {
        log.error("Erreur de stockage", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Service de stockage temporairement indisponible"));
    }

    /**
     * Convertit les {@link IllegalArgumentException} (ex. {@code Enum.valueOf}
     * sur une valeur inconnue passée dans une query string) en HTTP 400 propre.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Catch-all final : log la pile d'erreur côté serveur et renvoie un 500 générique
     * sans fuiter le détail technique au client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Erreur non gérée", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erreur interne du serveur"));
    }

    /**
     * Formate un {@link FieldError} en chaîne {@code "champ: message"} pour la
     * liste {@code errors} de la réponse standardisée.
     */
    private String formatFieldError(FieldError fieldError) {
        return "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
    }




}
