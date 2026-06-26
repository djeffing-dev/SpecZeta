import { User } from './user.model'; // Assuming you have a User model defined elsewhere

export interface OtpResponse {
    status: string; // e.g., "success"
    detail: string; // e.g., "Email vérifié avec succès."
    accessToken: string; // Access token
    refreshToken: string; // Refresh token
    expires_in: number; // Expiration time in seconds
    user: User; // User data
}