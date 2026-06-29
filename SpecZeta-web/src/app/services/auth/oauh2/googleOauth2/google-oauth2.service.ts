import { map, Observable, retry, tap } from 'rxjs';
import { API_ENDPOINTS } from './../../../../../const/api.constants';
import { HttpClient } from '@angular/common/http';
import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { STORAGE_KEYS } from '../../../../../const/storage.constants';
import { User } from '../../../../models/user.model';
import { AuthResponse } from '../../../../models/auth-response.model';
import { ApiResponse } from '../../../../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class GoogleOauth2Service {
  private platformId = inject(PLATFORM_ID);

  constructor(private http: HttpClient) { }

  getUrl(): Observable<any> {
    return this.http.get<any>(API_ENDPOINTS.GOOGLE_OAUTH.GET_URL)
      .pipe(
        map(res => res.url),
        tap(res => console.log("L'url de connexion a google : ", res)),
        retry(3)
      )
  }

  getToken(code: string): Observable<any> {
    return this.http.get<any>(API_ENDPOINTS.GOOGLE_OAUTH.GET_TOKEN(code))
      .pipe(
        retry(3),
        // map(res => res.data),
        tap(res => this.storeSession(res))
      )
  }

  storeSession(res: any): void {
    if (!isPlatformBrowser(this.platformId)) {
      return; // Pas de stockage possible côté serveur, on ignore
    }
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, res.accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, res.refreshToken);
    localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(res.user));
  }
}