export interface User {
  id: number;
  email: string;
  pseudo: string;
  photoUrl: string | null;
  ville: string | null;
  ratingMoyenne: number;
  nombreEvaluations: number;
}

export interface UserSummaryResponse {
  id: number;
  email?: string;
  pseudo: string;
  photoUrl: string | null;
  ville: string | null;
  ratingMoyenne: number | null;
  nombreEvaluations: number | null;
}

export interface UpdateProfileRequest{
  pseudo: string;
  ville: string | null;
  telephone: string | null;
  photoUrl: string | null;
  biographie: string | null;
  dateNaissance: string | null;
  adresse: string | null;
  codePostal: string | null;
  pays: string | null;
  siteWeb: string | null;
}

export interface UserProfileResponse{
  id: number;
  email: string;
  pseudo: string;
  photoUrl: string | null;
  ville: string | null;
  telephone: string | null;
  provider: string | null;
  emailVerified: boolean;
  ratingMoyenne: number | null;
  nombreEvaluations: number | null;
  createdAt: string;
  biographie: string | null;
  dateNaissance: string | null;
  adresse: string | null;
  codePostal: string | null;
  pays: string | null;
  siteWeb: string | null;
}

export interface DashboardResponse{
  annoncesActives: number;
  annoncesVendues: number;
  annoncesEnAttente: number;
  annoncesSuspendues: number;
  revenuTotal: number;
  conversationsNonLues: number;
  favorisRecus: number;
  ratingMoyenne: number;
  nombreEvaluations: number;
}

export interface RatingRequest{
  note: number;
  commentaire?: string | null;
  annonceId: number;
}

export interface RatingResponse{
  id: number;
  evalueId: number;
  evaluateurId: number;
  evaluateurPseudo: string;
  annonceId: number;
  note: number;
  commentaire: string | null;
}

