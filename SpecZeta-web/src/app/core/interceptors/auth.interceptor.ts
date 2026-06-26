import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../../services/auth/auth.service';
import { API_ENDPOINTS } from '../../../const/api.constants';

/**
 * Interceptor HTTP d'authentification.
 *
 * Responsabilités :
 *  1. Ajoute le header Authorization: Bearer <token> sur chaque requête.
 *  2. Sur erreur 401 :
 *       a. Si la requête est déjà vers le endpoint refresh → évite la boucle infinie,
 *          vide la session et laisse l'erreur remonter.
 *       b. Sinon → tente refreshAccessToken() :
 *            - Succès  : rejoue la requête originale avec le nouveau token.
 *            - Échec   : clearSession() + navigate('/login') déjà gérés
 *                        dans refreshAccessToken().
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth  = inject(AuthService);
  const token = auth.getAccessToken();

  // Attacher le token sur toutes les requêtes sortantes
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Ignorer les erreurs non-401
      if (error.status !== 401) {
        return throwError(() => error);
      }

      // Ne pas retenter si c'est déjà la requête de refresh
      // (évite la boucle infinie : 401 refresh → retry refresh → 401 → ...)
      const  refreshToken = localStorage.getItem('refresh_token')
      if (req.url.includes(API_ENDPOINTS.AUTH.REFRESH(refreshToken ?? ''))) {
        auth.clearSession();
        return throwError(() => error);
      }

      // Tenter le renouvellement du token
      return auth.refreshAccessToken().pipe(
        switchMap(success => {
          if (success) {
            // Rejouer la requête originale avec le nouveau accessToken
            const newToken  = auth.getAccessToken();
            const retryReq  = req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}` },
            });
            return next(retryReq);
          }

          // Échec du refresh : clearSession() + navigate déjà appelés
          return throwError(() => error);
        })
      );
    })
  );
};
