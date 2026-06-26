package com.djeffing.SpecZeta.domain.media.service;

import com.djeffing.SpecZeta.config.DropboxProperties;
import com.djeffing.SpecZeta.shared.exception.InvalidFileException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires des règles de validation de {@link DropboxStorageService}.
 *
 * <p>Volontairement centrés sur la couche validation : ces tests s'exécutent
 * sans avoir besoin de mocker {@code RestTemplate}, puisque les contrôles
 * échouent <em>avant</em> tout appel réseau. L'intégration HTTP avec Dropbox
 * doit être validée via un compte sandbox réel ou un test d'intégration séparé.</p>
 */
class DropboxStorageServiceTest {

    private DropboxStorageService service;

    /**
     * Initialise le service avec une configuration minimale valide.
     * Le {@code RestTemplate} interne ne sera pas sollicité par ces tests.
     */
    @BeforeEach
    void setUp() {
        DropboxProperties props = new DropboxProperties();
        props.setAppKey("test-key");
        props.setAppSecret("test-secret");
        props.setAccessToken("test-token");
        props.setRefreshToken("test-refresh");
        props.setEnvironment("dev");
        props.setMaxRetries(3);
        props.setTimeoutMs(5_000);
        service = new DropboxStorageService(props, new ObjectMapper());
    }

    @Test
    @DisplayName("uploadAnnoncePhoto refuse un fichier vide")
    void uploadPhotoRefuseFichierVide() {
        MultipartFile vide = new MockMultipartFile("file", "img.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> service.uploadAnnoncePhoto(vide, 1L, 1))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("vide");
    }

    @Test
    @DisplayName("uploadAnnoncePhoto refuse un type MIME non image")
    void uploadPhotoRefuseMauvaisType() {
        MultipartFile pdf = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "contenu".getBytes());

        assertThatThrownBy(() -> service.uploadAnnoncePhoto(pdf, 1L, 1))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("MIME");
    }

    @Test
    @DisplayName("uploadAnnoncePhoto refuse un fichier supérieur à 10 Mo")
    void uploadPhotoRefuseFichierTropLourd() {
        byte[] gros = new byte[11 * 1024 * 1024];
        MultipartFile lourd = new MockMultipartFile("file", "big.jpg", "image/jpeg", gros);

        assertThatThrownBy(() -> service.uploadAnnoncePhoto(lourd, 1L, 1))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("taille maximale");
    }

    @Test
    @DisplayName("uploadAnnoncePhoto accepte les types image officiels (jpeg, png, webp)")
    void uploadPhotoAccepteImagesValides() {
        // Le but ici n'est pas de valider l'appel Dropbox (RestTemplate non mocké)
        // mais simplement de s'assurer que la validation laisse passer ces MIME types.
        // L'appel suivant lèvera une exception réseau, pas InvalidFileException.
        for (String mime : new String[]{"image/jpeg", "image/png", "image/webp"}) {
            MultipartFile img = new MockMultipartFile("file", "img", mime, "data".getBytes());
            assertThatThrownBy(() -> service.uploadAnnoncePhoto(img, 1L, 1))
                    .isNotInstanceOf(InvalidFileException.class);
        }
    }

    @Test
    @DisplayName("uploadCertificationFile refuse un fichier supérieur à 5 Mo")
    void uploadCertifRefuseFichierTropLourd() {
        byte[] gros = new byte[6 * 1024 * 1024];
        MultipartFile lourd = new MockMultipartFile("file", "log.txt", "text/plain", gros);

        assertThatThrownBy(() -> service.uploadCertificationFile(lourd, 1L, "cpuz"))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("taille maximale");
    }

    @Test
    @DisplayName("uploadCertificationFile refuse un type MIME hors liste blanche")
    void uploadCertifRefuseMauvaisType() {
        MultipartFile image = new MockMultipartFile(
                "file", "screen.png", "image/png", "contenu".getBytes());

        assertThatThrownBy(() -> service.uploadCertificationFile(image, 1L, "cpuz"))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("MIME");
    }
}
