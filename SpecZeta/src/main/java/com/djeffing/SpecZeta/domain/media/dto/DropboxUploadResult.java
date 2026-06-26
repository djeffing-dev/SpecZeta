package com.djeffing.SpecZeta.domain.media.dto;

import java.time.Instant;

/**
 * Résultat immuable d'un upload Dropbox.
 *
 * <p>Renvoyé par les méthodes du {@code StorageService}. Le {@code dropboxPath}
 * est conservé en base pour permettre une suppression ultérieure ; le {@code sharedUrl}
 * est le seul champ exposé au client final (et toujours sous forme directe
 * {@code dl.dropboxusercontent.com?dl=1} pour éviter une redirection).</p>
 */
public record DropboxUploadResult(
        String dropboxPath,
        String sharedUrl,
        String fileName,
        long fileSizeBytes,
        String contentType,
        Instant uploadedAt
) {
}
