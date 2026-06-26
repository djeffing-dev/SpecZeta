import { Injectable } from '@angular/core';
import { HttpClient, HttpBackend } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, of, tap, map, catchError } from 'rxjs';

import { API_ENDPOINTS } from '../../../const/api.constants';
import { STORAGE_KEYS } from '../../../const/storage.constants';
import { AuthResponse, SignupPayload, SignupResponse } from '../../models/auth-response.model';
import { User } from '../../models/user.model';
import { ApiResponse } from '../../models/api-response.model';
import { Token } from '../../models/token.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  /**
   * Client HTTP qui bypasse les interceptors.
   * Utilisé exclusivement pour le refresh token afin d'éviter la
   * dépendance circulaire : interceptor → AuthService → HttpClient → interceptor.
   */
  private readonly refreshClient: HttpClient;

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
    backend: HttpBackend,
  ) {
    this.refreshClient = new HttpClient(backend);
  }

  // ------------------------------------------------------------------ Login
  /**
   * @param rememberMe  Si true → le backend étend les durées de vie des tokens
   */
  login(email: string, password: string, rememberMe = false): Observable<AuthResponse> {
    return this.http 
      .post<ApiResponse<AuthResponse>>(API_ENDPOINTS.AUTH.LOGIN, { email, password, rememberMe })
      .pipe(
        map(res => res.data),
        tap(res => this.storeSession(res))
      );
  }

  // ----------------------------------------------------------------- Signup
  signup(userData: SignupPayload): Observable<SignupResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(API_ENDPOINTS.AUTH.REGISTER, userData)
      .pipe(
        map(res => ({ success: res.success, message: res.message })),
        tap(() => {}) // On pourrait auto-login ici si on voulait
      );
  }

  // ----------------------------------------------------------------- Logout
  logout(): void {
    this.clearSession();
    this.router.navigate(['/login']);
  }

  clearSession(): void {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER_INFO);
  }

  storeSession(res: { accessToken: string; refreshToken: string; user: User }): void {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN,  res.accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, res.refreshToken);
    localStorage.setItem(STORAGE_KEYS.USER_INFO,     JSON.stringify(res.user));
  }

  // ----------------------------------------------------------- Token helpers
  getAccessToken(): string | null {
    return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
  }

  // ------------------------------------------------------------ User helpers
  getUserInfo(): User | null {
    const raw = localStorage.getItem(STORAGE_KEYS.USER_INFO);
    return raw ? (JSON.parse(raw) as User) : null;
  }

  isAuthenticated(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;
    return !this.isTokenExpired(token);
  }

  /**
   * Renouvelle l'accessToken en utilisant le refreshToken.
   */
  refreshAccessToken(): Observable<boolean> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken || this.isTokenExpired(refreshToken)) {
      this.clearSession();
      this.router.navigate(['/login']);
      return of(false);
    }

    return this.refreshClient
      .post<ApiResponse<Token>>(
        API_ENDPOINTS.AUTH.REFRESH(refreshToken),
        {}
      )
      .pipe(
        tap(res => {
          if (res.data) {
            localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, res.data.accessToken);
            if (res.data.RefreshToken) {
              localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, res.data.RefreshToken);
            }
          }
        }),
        map(() => true),
        catchError(() => {
          this.clearSession();
          this.router.navigate(['/login']);
          return of(false);
        }),
      );
  }

  isTokenExpired(token: string): boolean {
    const payload = this.decodeTokenPayload(token);
    if (!payload || typeof payload['exp'] !== 'number') {
      return true;
    }
    return payload['exp'] * 1000 < Date.now();
  }

  decodeTokenPayload(token: string): Record<string, unknown> | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;

      const base64  = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded  = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=');
      const decoded = atob(padded);
      return JSON.parse(decoded) as Record<string, unknown>;
    } catch {
      return null;
    }
  }
}
