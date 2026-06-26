package com.djeffing.SpecZeta.domain.media.service;

import com.djeffing.SpecZeta.domain.media.dto.DropboxUploadResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contrat d'abstraction pour le stockage des fichiers de la marketplace.
 *
 * <p>L'implémentation actuelle est {@code DropboxStorageService}, mais l'interface
 * permet de remplacer le backend (S3, local FS, …) sans toucher au code métier.</p>
 */
public interface StorageService {

    /**
     * Upload une photo d'annonce. Le fichier est renommé selon la convention
     * {@code {ordre}_{uuid6}_{timestamp}.{ext}} et stocké sous
     * {@code /marketplace/{env}/annonces/{annonceId}/photos/}.
     *
     * @param file     fichier multipart reçu du client (validation MIME + taille faite par l'impl)
     * @param annonceId identifiant de l'annonce propriétaire (utilisé dans le path)
     * @param ordre    position 1-based dans la galerie (utilisé dans le nom de fichier)
     * @return résultat immuable contenant le path Dropbox + URL publique directe
     */
    DropboxUploadResult uploadAnnoncePhoto(MultipartFile file, Long annonceId, int ordre);

    /**
     * Upload un fichier de certification benchmark (log CPU-Z, HWiNFO, etc.)
     * dans le dossier {@code certifications/} de l'annonce.
     *
     * @param file     fichier brut (txt, json, octet-stream)
     * @param annonceId identifiant de l'annonce
     * @param type     préfixe identifiant le type de benchmark (ex. {@code cpuz}, {@code hwinfo})
     */
    DropboxUploadResult uploadCertificationFile(MultipartFile file, Long annonceId, String type);

    /**
     * Supprime un fichier précis par son chemin Dropbox.
     * Si le fichier n'existe pas, log un warning mais ne lève pas d'exception
     * (suppression idempotente).
     */
    void deleteFile(String dropboxPath);

    /**
     * Supprime tout le dossier d'une annonce ({@code .../annonces/{id}/}) en une
     * seule opération. Utilisé pour le nettoyage cascade lors de la suppression
     * d'une annonce ; plus efficace que de supprimer chaque fichier individuellement.
     */
    void deleteAnnonceFolder(Long annonceId);

    /**
     * Régénère le lien partagé direct d'un fichier déjà uploadé (utile si
     * l'URL stockée en base est compromise ou révoquée côté Dropbox).
     */
    String refreshSharedLink(String dropboxPath);
}
