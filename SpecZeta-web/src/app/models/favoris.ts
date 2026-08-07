import { AnnonceListResponse } from './annoce';
export interface AddFavoriRequest{
    annonceId: number;
}

export interface FavoriResponse{
    id: number;
    createdAt: string;
    annonce: AnnonceListResponse;
}