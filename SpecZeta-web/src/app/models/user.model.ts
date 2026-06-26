export interface User {
  id: number;
  email: string;
  pseudo: string;
  photoUrl: string | null;
  ville: string | null;
  ratingMoyenne: number;
  nombreEvaluations: number;
}
