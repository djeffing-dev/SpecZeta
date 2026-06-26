import { User } from './user.model';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface SignupResponse {
  success: boolean;
  message: string;
}

export interface SignupPayload {
  email: string;
  pseudo: string;
  password: string;
  ville?: string;
}
