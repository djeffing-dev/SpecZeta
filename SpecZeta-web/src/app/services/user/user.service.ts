import { HttpClient } from '@angular/common/http';
import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { DashboardResponse, UpdateProfileRequest, UserProfileResponse } from '../../models/user.model';
import { API_ENDPOINTS } from '../../../const/api.constants';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly platformId = inject(PLATFORM_ID);
  constructor(private http: HttpClient) { }

  getMyprofile(): Observable<ApiResponse<UserProfileResponse>>{
    return this.http.get<ApiResponse<UserProfileResponse>>(API_ENDPOINTS.USERS.ME);
  }

  getDashboad(): Observable<ApiResponse<DashboardResponse>>{
    return this.http.get<ApiResponse<DashboardResponse>>(API_ENDPOINTS.USERS.DASHBOARD);

  }

  updateProfile(profile: UpdateProfileRequest): Observable<ApiResponse<UserProfileResponse>> {
    return this.http.put<ApiResponse<UserProfileResponse>>(API_ENDPOINTS.USERS.ME, profile);
  }

  uploadProfilUrl(file: File): Observable<ApiResponse<string>> {
    const formData = new FormData();
    formData.append('file', file)
    return this.http.post<ApiResponse<string>>(API_ENDPOINTS.USERS.UPLOAD_PROFIL_URL, formData);
  }

  getPublicProfile(userId: number): Observable<ApiResponse<UserProfileResponse>> {
    return this.http.get<ApiResponse<UserProfileResponse>>(API_ENDPOINTS.USERS.PROFILE(userId));
  }

  submitRating(userId: number, rating: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(API_ENDPOINTS.USERS.SUBMIT_RATING(userId), { rating });
  }


}
