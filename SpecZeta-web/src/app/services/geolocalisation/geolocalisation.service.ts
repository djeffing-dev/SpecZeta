import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, from, switchMap, map, catchError, of } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class GeolocationService {

  private readonly NOMINATIM_URL = 'https://nominatim.openstreetmap.org/reverse';
  private readonly platformId = inject(PLATFORM_ID);
  private readonly http = inject(HttpClient);

  getCityFromBrowser(): Observable<any | null> {
    // ✅ Garde SSR : navigator n'existe pas côté serveur
    if (!isPlatformBrowser(this.platformId)) {
      console.log('⚠️ SSR détecté — géolocalisation ignorée');
      return of(null);
    }

    return from(this.getBrowserPosition()).pipe(
      switchMap(position => {
        console.log('✅ Position obtenue :', position.coords.latitude, position.coords.longitude);
        return this.reverseGeocode(position.coords.latitude, position.coords.longitude).pipe(
          map(ville =>({
            ville,
            position 
          }))
        );
      }),
      catchError((err) => {
        console.error('❌ Erreur géolocalisation :', err.message);
        return of(null);
      })
    );
  }

  private getBrowserPosition(): Promise<GeolocationPosition> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Géolocalisation non supportée par ce navigateur'));
        return;
      }
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        timeout: 10000,
        maximumAge: 300000,
        enableHighAccuracy: false
      });
    });
  }

  private reverseGeocode(lat: number, lon: number): Observable<string | null> {
    const params = {
      lat: lat.toString(),
      lon: lon.toString(),
      format: 'json',
      'accept-language': 'fr'
    };

    return this.http.get<NominatimResponse>(this.NOMINATIM_URL, { params }).pipe(
      map(response => {
        console.log('✅ Réponse Nominatim :', response.address);
        const address = response.address;
        return address.city
            ?? address.town
            ?? address.village
            ?? address.municipality
            ?? null;
      }),
      catchError((err) => {
        console.error('❌ Erreur Nominatim :', err);
        return of(null);
      })
    );
  }
}

interface NominatimResponse {
  address: {
    city?: string;
    town?: string;
    village?: string;
    municipality?: string;
    state?: string;
    country?: string;
  };
}