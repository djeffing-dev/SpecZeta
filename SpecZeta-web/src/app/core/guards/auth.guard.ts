import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { Observable, of, switchMap } from 'rxjs';

import { AuthService } from '../../services/auth/auth.service';

/**
 * Guard d'authentification — 3 cas possibles :
 *
 * 1. Access token valide                → accès autorisé (synchrone)
 * 2. Access token expiré + refresh OK   → tentative de renouvellement (asynchrone)
 *      └─ succès  → accès autorisé
 *      └─ échec   → clearSession() déjà fait dans refreshAccessToken()  → /login
 * 3. Refresh token absent / expiré      → clearSession() + redirect /login (synchrone)
 */
export const authGuard: CanActivateFn = (): Observable<boolean | UrlTree> => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  // Cas 1 : access token encore valide
  if (auth.isAuthenticated()) {
    return of(true);
  }

  const refreshToken = auth.getRefreshToken();

  // Cas 3 : plus aucun token utilisable
  if (!refreshToken || auth.isTokenExpired(refreshToken)) {
    auth.clearSession();
    return of(router.createUrlTree(['/login']));
  }

  // Cas 2 : access expiré mais refresh valide → renouvellement
  return auth.refreshAccessToken().pipe(
    switchMap(success =>
      success
        ? of(true)
        : of(router.createUrlTree(['/login']))
      // Note : si success = false, clearSession() + navigate ont déjà
      // été appelés dans refreshAccessToken(), la UrlTree est un filet de sécurité.
    )
  );
};
