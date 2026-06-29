import { environment } from '../environments/environment';

const BASE = environment.apiUrl;

export const API_ENDPOINTS = {
  // Authentification
  AUTH: {
    LOGIN: `${BASE}/auth/login`,
    REGISTER: `${BASE}/auth/register`,
    REFRESH: (token: string) => `${BASE}/auth/refreshToken/${token}`,
  },

  OTP:{
    VERIFY_OTP: `${BASE}/otp/verify-otp`,
    RESEND_OTP: `${BASE}/otp/resend-otp`,
  },

  // Utilisateurs & Profils
  USERS: {
    ME: `${BASE}/users/me`,
    ME_DASHBOARD: `${BASE}/users/me/dashboard`,
    ME_ANNONCES: `${BASE}/users/me/annonces`,
    PROFILE: (id: number | string) => `${BASE}/users/${id}`,
    SUBMIT_RATING: (id: number | string) => `${BASE}/users/${id}/ratings`,
  },

  // Annonces (Catalogue, Gestion, Certification)
  ANNONCES: {
    LIST: `${BASE}/annonces`,
    DETAILS: (id: number | string) => `${BASE}/annonces/${id}`,
    CREATE: `${BASE}/annonces`,
    UPDATE: (id: number | string) => `${BASE}/annonces/${id}`,
    DELETE: (id: number | string) => `${BASE}/annonces/${id}`,
    UPDATE_STATUT: (id: number | string) => `${BASE}/annonces/${id}/statut`,
    SUBMIT_CERTIFICATION: (id: number | string) => `${BASE}/annonces/${id}/certification`,
    UPLOAD_MEDIAS: (id: number | string) => `${BASE}/annonces/${id}/medias`,
  },

  // Favoris
  FAVORIS: {
    LIST: `${BASE}/favoris`,
    ADD: `${BASE}/favoris`,
    REMOVE: (annonceId: number | string) => `${BASE}/favoris/${annonceId}`,
    EXISTS: (annonceId: number | string) => `${BASE}/favoris/${annonceId}/exists`,
  },

  // Alertes de recherche
  ALERTES: {
    LIST: `${BASE}/alertes`,
    CREATE: `${BASE}/alertes`,
    UPDATE: (id: number | string) => `${BASE}/alertes/${id}`,
    DELETE: (id: number | string) => `${BASE}/alertes/${id}`,
  },

  // Messagerie (Conversations & Messages)
  CONVERSATIONS: {
    LIST: `${BASE}/conversations`,
    START: `${BASE}/conversations`,
    MESSAGES: (id: number | string) => `${BASE}/conversations/${id}/messages`,
    SEND_MESSAGE: (id: number | string) => `${BASE}/conversations/${id}/messages`,
    MARK_AS_READ: (id: number | string) => `${BASE}/conversations/${id}/read`,
  },

  // Google Oauth2
  GOOGLE_OAUTH: {
    GET_URL: `${BASE}/auth/google/getUrl`,
    GET_TOKEN: (code:string) => `${BASE}/auth/google/getToken?code=${code}`,
  }
};
