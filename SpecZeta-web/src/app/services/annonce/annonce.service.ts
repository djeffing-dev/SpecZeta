import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../models/api-response.model';
import {
  AnnonceListParams,
  AnnonceListResponse,
  AnnonceRequest,
  AnnonceResponse,
  CertificationRequest,
  StatutAnnonce,
  UpdateAnnonceRequest,
} from '../../models/annoce';
import { API_ENDPOINTS } from '../../../const/api.constants';
import { PagedResponse } from '../../models/paged-response';

@Injectable({
  providedIn: 'root'
})
export class AnnonceService {
  private readonly platformId = inject(PLATFORM_ID);
  constructor(private http: HttpClient) { }

  /**
   * Crée une nouvelle annonce (statut initial `EN_ATTENTE`).
   * POST /api/annonces — authentification requise.
   */
  create(annonce: AnnonceRequest): Observable<ApiResponse<AnnonceResponse>> {
    return this.http.post<ApiResponse<AnnonceResponse>>(API_ENDPOINTS.ANNONCES.CREATE, annonce);
  }

  /**
   * Liste paginée des annonces actives (endpoint public), avec filtres optionnels.
   * GET /api/annonces
   */
  getPublic(params: AnnonceListParams = {}): Observable<ApiResponse<PagedResponse<AnnonceListResponse>>> {
    return this.http.get<ApiResponse<PagedResponse<AnnonceListResponse>>>(
      API_ENDPOINTS.ANNONCES.LIST,
      { params: this.buildParams(params) }
    );
  }

  /**
   * Détail complet d'une annonce (endpoint public).
   * GET /api/annonces/{id}
   */
  getById(id: number | string): Observable<ApiResponse<AnnonceResponse>> {
    return this.http.get<ApiResponse<AnnonceResponse>>(API_ENDPOINTS.ANNONCES.DETAILS(id));
  }

  /**
   * Met à jour une annonce existante (PATCH partiel applicatif : seuls les champs
   * fournis sont modifiés). Réservé au propriétaire.
   * PUT /api/annonces/{id}
   */
  update(id: number | string, annonce: UpdateAnnonceRequest): Observable<ApiResponse<AnnonceResponse>> {
    return this.http.put<ApiResponse<AnnonceResponse>>(API_ENDPOINTS.ANNONCES.UPDATE(id), annonce);
  }

  /**
   * Supprime définitivement une annonce et ses dépendances. Réservé au propriétaire.
   * DELETE /api/annonces/{id}
   */
  delete(id: number | string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(API_ENDPOINTS.ANNONCES.DELETE(id));
  }

  /**
   * Change le statut d'une annonce (ex. EN_ATTENTE → ACTIVE). Réservé au propriétaire.
   * PATCH /api/annonces/{id}/statut
   */
  updateStatut(id: number | string, statut: StatutAnnonce): Observable<ApiResponse<AnnonceResponse>> {
    return this.http.patch<ApiResponse<AnnonceResponse>>(
      API_ENDPOINTS.ANNONCES.UPDATE_STATUT(id),
      { statut }
    );
  }

  /**
   * Soumet une certification benchmark pour une annonce. Réservé au propriétaire.
   * POST /api/annonces/{id}/certification
   */
  submitCertification(id: number | string, request: CertificationRequest): Observable<ApiResponse<AnnonceResponse>> {
    return this.http.post<ApiResponse<AnnonceResponse>>(
      API_ENDPOINTS.ANNONCES.SUBMIT_CERTIFICATION(id),
      request
    );
  }

  /**
   * Upload (ou remplacement) des 3 à 5 photos d'une annonce (multipart/form-data).
   * La réponse contient la liste des URLs publiques dans l'ordre d'envoi.
   * POST /api/annonces/{id}/medias
   */
  uploadMedias(id: number | string, files: File[]): Observable<ApiResponse<string[]>> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    return this.http.post<ApiResponse<string[]>>(API_ENDPOINTS.ANNONCES.UPLOAD_MEDIAS(id), formData);
  }

  /**
   * Liste paginée des annonces du vendeur connecté (tous statuts par défaut).
   * GET /api/users/me/annonces
   */
  getMyAnnonces(
    statut?: StatutAnnonce,
    page?: number,
    size?: number,
    sort?: string
  ): Observable<ApiResponse<PagedResponse<AnnonceListResponse>>> {
    return this.http.get<ApiResponse<PagedResponse<AnnonceListResponse>>>(
      API_ENDPOINTS.USERS.ME_ANNONCES,
      { params: this.buildParams({ statut, page, size, sort }) }
    );
  }

  /**
   * Construit des HttpParams en ignorant les valeurs `null`/`undefined`.
   */
  private buildParams(source: { [key: string]: any }): HttpParams {
    let params = new HttpParams();
    Object.entries(source).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return params;
  }
}
