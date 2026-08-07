import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable, PLATFORM_ID } from '@angular/core';

import { API_ENDPOINTS } from '../../../const/api.constants';
import { ApiResponse } from '../../models/api-response.model';
import { AddFavoriRequest, FavoriResponse } from '../../models/favoris';
import { PagedResponse } from '../../models/paged-response';

@Injectable({
  providedIn: 'root'
})
export class FavorisService {
  private readonly platformId = inject(PLATFORM_ID);
  constructor(private http: HttpClient) { }

  /**
   * Page des favoris de l'utilisateur connecté.
   * Le backend applique `size=20` par défaut : on expose la pagination pour
   * pouvoir charger un lot plus large lors de l'hydratation des cœurs d'une liste.
   */
  getAll(page = 0, size = 20):Observable<ApiResponse<PagedResponse<FavoriResponse>>>{
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PagedResponse<FavoriResponse>>>(API_ENDPOINTS.FAVORIS.LIST, { params });
  }

  create(favorieRequest: AddFavoriRequest):Observable<ApiResponse<FavoriResponse>>{
    return this.http.post<ApiResponse<FavoriResponse>>(API_ENDPOINTS.FAVORIS.ADD, favorieRequest);
  }

  delete(annonceId: number):Observable<ApiResponse<void>>{
    return this.http.delete<ApiResponse<void>>(API_ENDPOINTS.FAVORIS.REMOVE(annonceId));
  }

  /** Le backend renvoie le booléen directement dans `data` (ApiResponse<Boolean>). */
  exists(annonceId: number):Observable<ApiResponse<boolean>>{
    return this.http.get<ApiResponse<boolean>>(API_ENDPOINTS.FAVORIS.EXISTS(annonceId));
  }


}
