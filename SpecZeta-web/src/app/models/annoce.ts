import { UserSummaryResponse } from "./user.model";

export interface RawDataJson {
    cores: number;
    baseFreqMhz: number;
}

export interface FicheTechnique {
    modele: string;
    marque: string;
    processeur: string;
    gpu: string;
    ramGo: number;
    stockageGo: number;
    typeStockage: string;
    ecranTaille: string; // Contient les guillemets ex: 15.6"
    ecranResolution: string;
    socket: string;
    sourceApi: string; // Tu pourrais aussi utiliser un type strict ou un enum si les sources sont fixes (ex: 'TECHPOWERUP' | 'AUTRE')
    rawDataJson: RawDataJson;
}

export interface AnnonceRequest {
    titre: string;
    description: string;
    prix: number;
    categorie: string;     // Idéalement un enum si tu as une liste fixe (ex: CategorieEnum)
    etat: string;          // Idéalement un enum (ex: 'NEUF' | 'TRES_BON' | 'BON'...)
    modeRemise: string;    // Idéalement un enum (ex: 'MAIN_PROPRE' | 'ENVOI' | 'LES_DEUX')
    latitude: number;
    longitude: number;
    ville:string;
    ficheTechnique: FicheTechnique;
}

export interface UpdateAnnonceRequest {
    titre?: string;
    description?: string;
    prix?: number;
    categorie?: string;
    etat?: string;
    modeRemise?: string;
    latitude?: number;
    longitude?: number;
    ficheTechnique?: FicheTechnique;
}

export interface UpdateStatutRequest {
    statut: StatutAnnonce;
}

export interface CertificationRequest {
    typeBenchmark: string;   // ex: 'GEEKBENCH_6', '3DMARK_TIMESPY', 'CPU_Z'...
    urlBenchmark?: string;
    scoreMonocoeur?: number;
    scoreMulticoeur?: number;
    scoreGpu?: number;
    logFileUrl?: string;
    logDataJson?: string;
}

/**
 * Filtres optionnels pour la liste publique des annonces (GET /annonces).
 * Tous combinables, plus la pagination Spring Data.
 */
export interface AnnonceListParams {
    categorie?: CategorieAnnonce;
    etat?: EtatEsthetique;
    prixMin?: number;
    prixMax?: number;
    certifieeOnly?: boolean;
    page?: number;
    size?: number;
    sort?: string; // ex: 'createdAt,desc'
}

export enum CategorieAnnonce {
    ORDINATEUR_PORTABLE = 'ORDINATEUR_PORTABLE',
    ORDINATEUR_FIXE = 'ORDINATEUR_FIXE',
    COMPOSANT_PC = 'COMPOSANT_PC',
    PERIPHERIQUE = 'PERIPHERIQUE',
    ECRAN = 'ECRAN',
    SMARTPHONE = 'SMARTPHONE',
    TABLETTE = 'TABLETTE',
    CONSOLE = 'CONSOLE',
    ACCESSOIRE_GAMING = 'ACCESSOIRE_GAMING',
    RESEAU = 'RESEAU',
    AUTRE = 'AUTRE'
}

export enum EtatEsthetique {
   NEUF= 'NEUF',
   TRES_BON= 'TRES_BON',
   BON='BON',
   POUR_PIECES='POUR_PIECES'
}

export type ModeRemise = 'MAIN_PROPRE' | 'ENVOI' | 'LES_DEUX';

export enum StatutAnnonce {
    ACTIVE= 'ACTIVE',
    VENDUE= "VENDUE",
    EN_ATTENTE= 'EN_ATTENTE',
    SUSPENDUE= 'SUSPENDUE'
}


export interface FicheTechniqueResponse {
    modele: string;
    marque: string;
    processeur: string;
    gpu: string;
    ramGo: number;
    stockageGo: number;
    typeStockage: string;
    ecranTaille: string;
    ecranResolution: string;
    // Reprend les champs de ta fiche technique précédente
}

export interface CertificationResponse {
    id: number;
    scoreBenchmark: number;
    dateCertification: string;
    valide: boolean;
}

export interface AnnonceMediaResponse {
    id: number;
    dropboxUrl: string;
    ordreAffichage: number;
    principale: boolean;
}

export interface AnnonceResponse {
    id: number;                  // Long en Java -> number en TypeScript
    titre: string;
    description: string;
    prix: number;                // BigDecimal en Java -> number en TypeScript
    categorie: CategorieAnnonce;
    etat: EtatEsthetique;
    modeRemise: ModeRemise;
    statut: StatutAnnonce;
    certifiee: boolean;          // Boolean -> boolean
    latitude: number;            // Double -> number
    longitude: number;           // Double -> number

    vendeur: UserSummaryResponse;
    // Les objets qui peuvent être null côté backend sont marqués avec "| null"
    ficheTechnique: FicheTechniqueResponse | null;
    certification: CertificationResponse | null;

    medias: AnnonceMediaResponse[];// List<AnnonceMediaResponse> -> Tableau []
    createdAt: string;           // LocalDateTime -> string (format ISO-8601)
    updatedAt: string;           // LocalDateTime -> string (format ISO-8601)
}

export interface AnnonceListResponse{
    id: number;
    titre: string;
    prix: number;
    categorie: CategorieAnnonce;
    etat: EtatEsthetique;
    status: StatutAnnonce;
    certifiee: boolean;
    photoPrincipaleUrl: string;
    vendeurPseudo: string;
    vendeurVille: string;
    ville: string;
    createdAt: Date
}