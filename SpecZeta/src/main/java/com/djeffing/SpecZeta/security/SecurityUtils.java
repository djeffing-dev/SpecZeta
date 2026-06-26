package com.djeffing.SpecZeta.security;

import com.djeffing.SpecZeta.shared.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Récupère le principal applicatif courant depuis le {@link SecurityContextHolder}.
     * Lance {@link UnauthorizedException} si aucun utilisateur n'est authentifié
     * (cas où la route est protégée mais l'appelant n'a pas fourni de JWT valide).
     */
    public static CustomUserPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new UnauthorizedException("Authentification requise");
        }
        return principal;
    }

    /**
     * Raccourci pour obtenir l'id base de données de l'utilisateur courant
     * sans avoir à manipuler le principal.
     */
    public static Long getCurrentUserId() {
        return getCurrentPrincipal().getId();
    }

    /**
     * Raccourci pour obtenir l'email de l'utilisateur courant.
     */
    public static String getCurrentUserEmail() {
        return getCurrentPrincipal().getEmail();
    }

    /**
     * Indique si la requête courante porte une authentification valide.
     * Utile dans les contrôleurs qui exposent un comportement différencié
     * pour utilisateurs anonymes vs. authentifiés.
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof CustomUserPrincipal;
    }
}
