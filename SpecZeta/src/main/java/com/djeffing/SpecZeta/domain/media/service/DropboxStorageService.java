package com.djeffing.SpecZeta.domain.media.service;

import com.djeffing.SpecZeta.config.DropboxProperties;
import com.djeffing.SpecZeta.domain.media.dto.DropboxUploadResult;
import com.djeffing.SpecZeta.shared.exception.InvalidFileException;
import com.djeffing.SpecZeta.shared.exception.StorageException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class DropboxStorageService implements StorageService {

    private static final String API_URL = "https://api.dropboxapi.com/2";
    private static final String CONTENT_URL = "https://content.dropboxapi.com/2";
    private static final String TOKEN_URL = "https://api.dropbox.com/oauth2/token";

    private static final Set<String> IMAGE_TYPES_AUTORISES =
            Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> CERTIF_TYPES_AUTORISES =
            Set.of("text/plain", "application/json", "application/octet-stream");

    private static final long MAX_PHOTO_BYTES = 10L * 1024 * 1024;
    private static final long MAX_CERTIF_BYTES = 5L * 1024 * 1024;

    private final DropboxProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Référence atomique vers l'access token courant. Initialisée depuis
     * la config, puis remplacée par les appels de refresh quand Dropbox
     * renvoie 401. Atomique car potentiellement modifiée et lue depuis plusieurs
     * threads (requêtes concurrentes).
     */
    private final AtomicReference<String> currentAccessToken;

    /**
     * Construit le service. Crée le {@link RestTemplate} avec les timeouts
     * configurés et initialise la référence d'access token.
     */
    public DropboxStorageService(DropboxProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.currentAccessToken = new AtomicReference<>(properties.getAccessToken());

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getTimeoutMs());
        factory.setReadTimeout((int) properties.getTimeoutMs());
        this.restTemplate = new RestTemplate(factory);
    }

    // ============================== UPLOAD PHOTOS ============================== //

    /**
     * {@inheritDoc}
     * <p>Pipeline : validation → construction du chemin → upload binaire →
     * création du lien partagé → résultat immuable.</p>
     */
    @Override
    public DropboxUploadResult uploadAnnoncePhoto(MultipartFile file, Long annonceId, int ordre) {
        validateFile(file, IMAGE_TYPES_AUTORISES, MAX_PHOTO_BYTES, "photo");
        String ext = extensionFor(file.getContentType());
        String fileName = "%d_%s_%d.%s".formatted(ordre, shortUuid(), System.currentTimeMillis(), ext);
        String path = "%s/%s/annonces/%d/photos/%s"
                .formatted(properties.getBasePath(), properties.getEnvironment(), annonceId, fileName);
        return doUpload(file, path, fileName, annonceId);
    }

    /**
     * {@inheritDoc}
     * <p>Mêmes étapes que {@link #uploadAnnoncePhoto} mais avec dossier
     * {@code certifications/} et plafonds plus stricts (5 Mo).</p>
     */
    @Override
    public DropboxUploadResult uploadCertificationFile(MultipartFile file, Long annonceId, String type) {
        validateFile(file, CERTIF_TYPES_AUTORISES, MAX_CERTIF_BYTES, "certification");
        String ext = extensionFor(file.getContentType());
        String safeType = type == null ? "log" : type.toLowerCase().replaceAll("[^a-z0-9]", "");
        String fileName = "%s_%s_%d.%s".formatted(safeType, shortUuid(), System.currentTimeMillis(), ext);
        String path = "%s/%s/annonces/%d/certifications/%s"
                .formatted(properties.getBasePath(), properties.getEnvironment(), annonceId, fileName);
        return doUpload(file, path, fileName, annonceId);
    }

    /**
     * Cœur de l'upload : appelle Dropbox content API puis crée le lien partagé.
     * Sépare l'orchestration de la validation pour rester testable.
     */
    private DropboxUploadResult doUpload(MultipartFile file, String path, String fileName, Long annonceId) {
        try {
            uploadBinary(file, path);
            String sharedUrl = createSharedLink(path);
            log.info("[Dropbox] Upload OK | path={} | size={}KB | annonce={}",
                    path, file.getSize() / 1024, annonceId);
            return new DropboxUploadResult(
                    path,
                    sharedUrl,
                    fileName,
                    file.getSize(),
                    file.getContentType(),
                    Instant.now());
        } catch (StorageException | InvalidFileException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[Dropbox] Upload FAILED | annonce={} | path={}", annonceId, path, ex);
            throw new StorageException("Échec de l'upload Dropbox", ex);
        }
    }

    /**
     * Effectue l'appel HTTP {@code /files/upload} avec gestion du retry et
     * basculement en mode {@code overwrite} si Dropbox renvoie 409 (le chemin
     * existe déjà — peut arriver lors d'un retry partiellement réussi).
     */
    private void uploadBinary(MultipartFile file, String path) throws IOException {
        ObjectNode args = objectMapper.createObjectNode()
                .put("path", path)
                .put("mode", "add")
                .put("autorename", false)
                .put("mute", true);

        try {
            postBinaryWithRetry(CONTENT_URL + "/files/upload", args.toString(), file.getBytes());
        } catch (HttpClientErrorException.Conflict conflict) {
            log.warn("[Dropbox] Path existe déjà, on bascule en overwrite | path={}", path);
            args.put("mode", "overwrite");
            postBinaryWithRetry(CONTENT_URL + "/files/upload", args.toString(), file.getBytes());
        }
    }

    /**
     * Crée un lien partagé direct pour le fichier fraîchement uploadé.
     * Si Dropbox indique qu'un lien existe déjà ({@code shared_link_already_exists}),
     * on appelle {@code list_shared_links} pour récupérer l'URL existante.
     * Le lien est ensuite transformé en URL de téléchargement direct.
     */
    private String createSharedLink(String path) {
        ObjectNode payload = objectMapper.createObjectNode()
                .put("path", path);
        payload.set("settings", objectMapper.createObjectNode()
                .set("requested_visibility", objectMapper.createObjectNode().put(".tag", "public")));

        try {
            JsonNode response = postJsonWithRetry(
                    API_URL + "/sharing/create_shared_link_with_settings",
                    payload.toString());
            return toDirectDownloadUrl(response.get("url").asText());
        } catch (HttpClientErrorException.Conflict conflict) {
            log.warn("[Dropbox] Lien partagé déjà existant, récupération | path={}", path);
            return fetchExistingSharedLink(path);
        }
    }

    /**
     * Récupère le lien partagé existant via {@code /sharing/list_shared_links}
     * quand la création initiale a échoué en 409.
     */
    private String fetchExistingSharedLink(String path) {
        ObjectNode payload = objectMapper.createObjectNode()
                .put("path", path)
                .put("direct_only", true);
        JsonNode response = postJsonWithRetry(API_URL + "/sharing/list_shared_links", payload.toString());
        JsonNode links = response.get("links");
        if (links != null && links.isArray() && !links.isEmpty()) {
            return toDirectDownloadUrl(links.get(0).get("url").asText());
        }
        throw new StorageException("Aucun lien partagé récupérable pour " + path);
    }

    /**
     * Transforme l'URL Dropbox standard (www.dropbox.com…?dl=0) en URL de
     * téléchargement direct (dl.dropboxusercontent.com…?dl=1) qui sert le
     * fichier sans page d'aperçu intermédiaire.
     */
    private String toDirectDownloadUrl(String url) {
        return url.replace("www.dropbox.com", "dl.dropboxusercontent.com")
                .replace("?dl=0", "?dl=1");
    }

    // ============================== DELETE ============================== //

    /**
     * {@inheritDoc}
     * <p>Tolère le cas où le chemin n'existe pas (Dropbox renvoie 409 avec
     * {@code path/not_found}) : log un warning et n'interrompt pas le flux
     * (ex. nettoyage cascade sur une annonce dont les fichiers ont déjà été supprimés).</p>
     */
    @Override
    public void deleteFile(String dropboxPath) {
        ObjectNode payload = objectMapper.createObjectNode().put("path", dropboxPath);
        try {
            postJsonWithRetry(API_URL + "/files/delete_v2", payload.toString());
            log.info("[Dropbox] Suppression OK | path={}", dropboxPath);
        } catch (HttpClientErrorException.Conflict conflict) {
            String body = conflict.getResponseBodyAsString();
            if (body != null && body.contains("path/not_found")) {
                log.warn("[Dropbox] Path not found, skip delete | path={}", dropboxPath);
                return;
            }
            throw new StorageException("Conflit suppression Dropbox : " + body, conflict);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAnnonceFolder(Long annonceId) {
        String folder = "%s/%s/annonces/%d".formatted(
                properties.getBasePath(), properties.getEnvironment(), annonceId);
        log.info("[Dropbox] Suppression du dossier complet | path={}", folder);
        deleteFile(folder);
    }

    // ============================== SHARED LINK REFRESH ============================== //

    /**
     * {@inheritDoc}
     */
    @Override
    public String refreshSharedLink(String dropboxPath) {
        return createSharedLink(dropboxPath);
    }

    // ============================== HTTP HELPERS ============================== //

    /**
     * Envoie une requête JSON avec retry exponentiel et refresh automatique
     * de l'access token sur 401. Centralise toute la logique technique HTTP
     * pour que les méthodes métier restent lisibles.
     */
    private JsonNode postJsonWithRetry(String url, String jsonBody) {
        return executeWithRetry(() -> {
            HttpHeaders headers = jsonAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return parseJson(response.getBody());
        });
    }

    /**
     * Variante pour les uploads binaires (content API). Le corps est un tableau
     * d'octets et le payload JSON passe en en-tête {@code Dropbox-API-Arg}.
     */
    private void postBinaryWithRetry(String url, String dropboxApiArg, byte[] body) {
        executeWithRetry(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(currentAccessToken.get());
            headers.set("Dropbox-API-Arg", dropboxApiArg);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return null;
        });
    }

    /**
     * Boucle de retry centrale.
     * <ul>
     *   <li>Sur 401, refresh le token et retente immédiatement (sans compter cette tentative).</li>
     *   <li>Sur 5xx ou timeout, attend {@code 500ms × 2^tentative} et retente.</li>
     *   <li>Sur 4xx (sauf 401 unique), propage immédiatement — inutile de réessayer.</li>
     * </ul>
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> action) {
        boolean refreshedOnce = false;
        Exception lastError = null;
        for (int attempt = 0; attempt < properties.getMaxRetries(); attempt++) {
            try {
                return action.get();
            } catch (HttpClientErrorException.Unauthorized unauthorized) {
                if (refreshedOnce) {
                    throw new StorageException(
                            "Token Dropbox toujours rejeté après refresh", unauthorized);
                }
                log.info("[Dropbox] 401 reçu, refresh de l'access token");
                refreshAccessToken();
                refreshedOnce = true;
                attempt--; // Le refresh ne consomme pas une tentative
            } catch (HttpClientErrorException clientError) {
                // 4xx autre que 401 : ne pas retry
                throw clientError;
            } catch (HttpServerErrorException | ResourceAccessException retryable) {
                lastError = retryable;
                long backoffMs = 500L * (1L << attempt);
                log.warn("[Dropbox] Échec tentative {} ({}), retry dans {}ms",
                        attempt + 1, retryable.getClass().getSimpleName(), backoffMs);
                sleep(backoffMs);
            }
        }
        log.error("[Dropbox] Échec après {} tentatives", properties.getMaxRetries(), lastError);
        throw new StorageException(
                "Échec Dropbox après " + properties.getMaxRetries() + " tentatives", lastError);
    }

    /**
     * Refresh l'access token via le flow OAuth2 {@code grant_type=refresh_token}.
     * Met à jour {@link #currentAccessToken} de façon atomique pour les requêtes suivantes.
     */
    private synchronized void refreshAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", properties.getRefreshToken());
        body.add("client_id", properties.getAppKey());
        body.add("client_secret", properties.getAppSecret());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    TOKEN_URL,
                    new HttpEntity<>(body, headers),
                    String.class);
            JsonNode payload = parseJson(response.getBody());
            String newToken = payload.get("access_token").asText();
            currentAccessToken.set(newToken);
            log.info("[Dropbox] Access token refreshé avec succès");
        } catch (Exception ex) {
            throw new StorageException("Impossible de rafraîchir le token Dropbox", ex);
        }
    }

    /**
     * Construit les en-têtes standards d'une requête JSON authentifiée Dropbox.
     */
    private HttpHeaders jsonAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentAccessToken.get());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Parse une chaîne JSON en {@link JsonNode}, en encapsulant l'erreur sous
     * forme de {@link StorageException} pour ne pas faire fuiter d'IOException.
     */
    private JsonNode parseJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (IOException ex) {
            throw new StorageException("Réponse Dropbox illisible", ex);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new StorageException("Retry interrompu", ex);
        }
    }

    // ============================== VALIDATION ============================== //

    /**
     * Validation centralisée d'un fichier multipart avant upload :
     * non vide, type MIME dans la liste blanche, taille sous le plafond.
     */
    private void validateFile(MultipartFile file, Set<String> typesAutorises, long maxBytes, String libelle) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Le fichier " + libelle + " est vide");
        }
        String contentType = file.getContentType();
        if (contentType == null || !typesAutorises.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "Type MIME non supporté pour " + libelle + " : " + contentType);
        }
        if (file.getSize() > maxBytes) {
            throw new InvalidFileException(
                    "Le fichier " + libelle + " dépasse la taille maximale de " + (maxBytes / 1024 / 1024) + " Mo");
        }
    }

    /**
     * Déduit l'extension de fichier depuis le ContentType (jamais depuis le nom
     * original — protection anti path-traversal et anti-XSS sur le path Dropbox).
     */
    private String extensionFor(String contentType) {
        if (contentType == null) return "bin";
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "text/plain" -> "txt";
            case "application/json" -> "json";
            default -> "bin";
        };
    }

    /**
     * Génère un identifiant court de 6 caractères depuis un UUID,
     * utilisé pour rendre les noms de fichiers uniques tout en restant lisibles.
     */
    private String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
