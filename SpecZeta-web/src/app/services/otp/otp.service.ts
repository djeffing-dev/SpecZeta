import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_ENDPOINTS } from '../../../const/api.constants';
import { OtpResponse } from '../../models/otp.model';

@Injectable({ providedIn: 'root' })
export class OtpService {

  constructor(private readonly http: HttpClient) {}

  /**
   * Vérifie le code OTP saisi par l'utilisateur.
   * Le backend attend { email, code } — champ "code", pas "otp".
   * En cas de succès, retourne les tokens JWT + infos utilisateur.
   */
  verifyOtp(data: { email: string; plainCode: string }): Observable<OtpResponse> {
    return this.http.post<OtpResponse>(API_ENDPOINTS.OTP.VERIFY_OTP, data);
  }

  /**
   * Génère et renvoie un nouveau code OTP par email.
   * Invalide automatiquement les anciens codes côté backend.
   */
  resendOtp(email: string): Observable<{ detail: string }> {
    return this.http.post<{ detail: string }>(API_ENDPOINTS.OTP.RESEND_OTP,  email );
  }
}
