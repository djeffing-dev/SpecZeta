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

