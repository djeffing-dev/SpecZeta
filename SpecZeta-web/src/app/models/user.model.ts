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
  username: string;
  avatarUrl?: string; // Optionnel selon ton implémentation
  // Ajoute ici les autres champs de ton UserSummaryResponse
}

