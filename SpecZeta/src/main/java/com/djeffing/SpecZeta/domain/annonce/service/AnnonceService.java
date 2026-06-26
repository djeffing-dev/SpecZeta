package com.djeffing.SpecZeta.domain.annonce.service;

import com.djeffing.SpecZeta.domain.annonce.dto.AnnonceListResponse;
import com.djeffing.SpecZeta.domain.annonce.dto.AnnonceResponse;
import com.djeffing.SpecZeta.domain.annonce.dto.CertificationRequest;
import com.djeffing.SpecZeta.domain.annonce.dto.CreateAnnonceRequest;
import com.djeffing.SpecZeta.domain.annonce.dto.UpdateAnnonceRequest;
import com.djeffing.SpecZeta.domain.annonce.entity.Annonce;
import com.djeffing.SpecZeta.domain.annonce.entity.AnnonceMedia;
import com.djeffing.SpecZeta.domain.annonce.entity.CertificationBenchmark;
import com.djeffing.SpecZeta.domain.annonce.entity.FicheTechnique;
import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import com.djeffing.SpecZeta.domain.annonce.enums.EtatEsthetique;
import com.djeffing.SpecZeta.domain.annonce.enums.StatutAnnonce;
import com.djeffing.SpecZeta.domain.annonce.mapper.AnnonceMapper;
import com.djeffing.SpecZeta.domain.annonce.repository.AnnonceMediaRepository;
import com.djeffing.SpecZeta.domain.annonce.repository.AnnonceRepository;
import com.djeffing.SpecZeta.domain.media.dto.DropboxUploadResult;
import com.djeffing.SpecZeta.domain.media.service.StorageService;
import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import com.djeffing.SpecZeta.shared.exception.BadRequestException;
import com.djeffing.SpecZeta.shared.exception.InvalidFileException;
import com.djeffing.SpecZeta.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class AnnonceService {

    private static final int MIN_PHOTOS_REQUISES = 3;
    private static final int MAX_PHOTOS_AUTORISEES = 5;
    private static final Set<String> BENCHMARK_DOMAINS_AUTORISES = Set.of(
            "geekbench.com", "browser.geekbench.com",
            "3dmark.com", "www.3dmark.com",
            "userbenchmark.com",
            "cpu-z.com", "valid.x86.fr"
    );

    private final AnnonceRepository annonceRepository;
    private final AnnonceMediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    private final AnnonceMapper annonceMapper;

    public AnnonceService(AnnonceRepository annonceRepository,
                          AnnonceMediaRepository mediaRepository,
                          UserRepository userRepository,
                          StorageService storageService,
                          AnnonceMapper annonceMapper) {
        this.annonceRepository = annonceRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.annonceMapper = annonceMapper;
    }

    /**
     * Crée une nouvelle annonce pour un vendeur donné. L'annonce est créée en
     * statut {@code EN_ATTENTE} : elle ne sera visible publiquement qu'après upload
     * de 3 à 5 photos (passage en {@code ACTIVE} via {@link #updateStatut}).
     * Provisionne aussi la fiche technique si elle est fournie dans la requête.
     */
    @Transactional
    public AnnonceResponse create(Long vendeurId, CreateAnnonceRequest request) {
        User vendeur = userRepository.findById(vendeurId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", vendeurId));

        Annonce annonce = Annonce.builder()
                .vendeur(vendeur)
                .titre(request.getTitre())
                .description(request.getDescription())
                .prix(request.getPrix())
                .categorie(request.getCategorie())
                .etat(request.getEtat())
                .modeRemise(request.getModeRemise())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .statut(StatutAnnonce.EN_ATTENTE)
                .certifiee(false)
                .build();

        if (request.getFicheTechnique() != null) {
            FicheTechnique fiche = annonceMapper.toEntity(request.getFicheTechnique());
            annonce.attachFicheTechnique(fiche);
        }

        Annonce saved = annonceRepository.save(annonce);
        log.info("Annonce créée : id={}, vendeur={}", saved.getId(), vendeurId);
        return annonceMapper.toResponse(saved);
    }

    /**
     * Met à jour une annonce existante. Vérifie que l'appelant est bien le vendeur
     * propriétaire et applique un PATCH partiel : seuls les champs non-null de la
     * requête sont écrasés. La fiche technique est remplacée intégralement si fournie.
     *
     * @throws AccessDeniedException si l'appelant n'est pas le propriétaire
     */
    @Transactional
    public AnnonceResponse update(Long vendeurId, Long annonceId, UpdateAnnonceRequest request) {
        Annonce annonce = findOwned(vendeurId, annonceId);

        if (request.getTitre() != null) annonce.setTitre(request.getTitre());
        if (request.getDescription() != null) annonce.setDescription(request.getDescription());
        if (request.getPrix() != null) annonce.setPrix(request.getPrix());
        if (request.getCategorie() != null) annonce.setCategorie(request.getCategorie());
        if (request.getEtat() != null) annonce.setEtat(request.getEtat());
        if (request.getModeRemise() != null) annonce.setModeRemise(request.getModeRemise());
        if (request.getLatitude() != null) annonce.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) annonce.setLongitude(request.getLongitude());

        if (request.getFicheTechnique() != null) {
            FicheTechnique nouvelle = annonceMapper.toEntity(request.getFicheTechnique());
            annonce.attachFicheTechnique(nouvelle);
        }

        return annonceMapper.toResponse(annonceRepository.save(annonce));
    }

    /**
     * Supprime définitivement une annonce et tout son contenu cascade
     * (médias DB, fiche technique, certification, conversations).
     * Après suppression en base, nettoie aussi le dossier Dropbox de l'annonce
     * pour éviter de laisser des fichiers orphelins coûter de l'espace.
     *
     * @throws AccessDeniedException si l'appelant n'est pas le propriétaire
     */
    @Transactional
    public void delete(Long vendeurId, Long annonceId) {
        Annonce annonce = findOwned(vendeurId, annonceId);
        annonceRepository.delete(annonce);
        try {
            storageService.deleteAnnonceFolder(annonceId);
        } catch (Exception ex) {
            // Volontairement non bloquant : la suppression DB a réussi.
            // L'incident est loggé pour permettre un nettoyage manuel ultérieur.
            log.warn("Suppression Dropbox échouée pour annonce {} : {}", annonceId, ex.getMessage());
        }
        log.info("Annonce supprimée : id={}, vendeur={}", annonceId, vendeurId);
    }

    /**
     * Upload entre 3 et 5 photos pour une annonce. Cette opération
     * <strong>remplace l'intégralité</strong> des photos existantes (suppression
     * Dropbox + DB des anciennes, puis upload + persistance des nouvelles)
     * pour garantir un état cohérent côté galerie. Vérifie au préalable
     * la propriété de l'annonce.
     *
     * @throws InvalidFileException  si le nombre de fichiers est hors fourchette [3, 5]
     * @throws AccessDeniedException si l'appelant n'est pas le vendeur
     */
    @Transactional
    public List<String> uploadMedias(Long vendeurId, Long annonceId, List<MultipartFile> files) {
        if (files == null || files.size() < MIN_PHOTOS_REQUISES || files.size() > MAX_PHOTOS_AUTORISEES) {
            throw new InvalidFileException(
                    "Une annonce doit contenir entre %d et %d photos (fournies : %d)"
                            .formatted(MIN_PHOTOS_REQUISES, MAX_PHOTOS_AUTORISEES, files == null ? 0 : files.size()));
        }

        Annonce annonce = findOwned(vendeurId, annonceId);

        // Suppression des anciennes photos : DB d'abord, puis Dropbox (best effort).
        List<AnnonceMedia> existants = mediaRepository.findByAnnonceIdOrderByOrdreAsc(annonceId);
        mediaRepository.deleteByAnnonceId(annonceId);
        for (AnnonceMedia ancien : existants) {
            try {
                storageService.deleteFile(ancien.getDropboxPath());
            } catch (Exception ex) {
                log.warn("Échec suppression ancienne photo {} : {}", ancien.getDropboxPath(), ex.getMessage());
            }
        }

        // Upload des nouvelles photos en conservant l'ordre.
        List<AnnonceMedia> nouveaux = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            int ordre = i + 1;
            DropboxUploadResult result = storageService.uploadAnnoncePhoto(files.get(i), annonceId, ordre);
            nouveaux.add(AnnonceMedia.builder()
                    .annonce(annonce)
                    .dropboxUrl(result.sharedUrl())
                    .dropboxPath(result.dropboxPath())
                    .ordre(ordre)
                    .uploadedAt(LocalDateTime.ofInstant(result.uploadedAt(), ZoneOffset.UTC))
                    .build());
        }
        mediaRepository.saveAll(nouveaux);

        log.info("Annonce {} : {} photos uploadées", annonceId, nouveaux.size());
        return nouveaux.stream().map(AnnonceMedia::getDropboxUrl).toList();
    }

    /**
     * Récupère le détail public d'une annonce par son id.
     * Lecture seule, accessible à tous (la sécurité côté contrôleur ouvre le GET).
     */
    @Transactional(readOnly = true)
    public AnnonceResponse findById(Long annonceId) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", annonceId));
        return annonceMapper.toResponse(annonce);
    }

    /**
     * Liste paginée d'annonces avec filtres basiques (catégorie, état, fourchette
     * de prix). Toujours restreint au statut {@code ACTIVE} pour le feed public.
     * Pour la recherche avancée (texte libre, géo), voir le module Elasticsearch (Étape 4).
     */
    @Transactional(readOnly = true)
    public Page<AnnonceListResponse> list(CategorieAnnonce categorie,
                                          EtatEsthetique etat,
                                          BigDecimal prixMin,
                                          BigDecimal prixMax,
                                          Boolean certifieeOnly,
                                          Pageable pageable) {
        Specification<Annonce> spec = (root, query, cb) -> cb.equal(root.get("statut"), StatutAnnonce.ACTIVE);
        if (categorie != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("categorie"), categorie));
        }
        if (etat != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("etat"), etat));
        }
        if (prixMin != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("prix"), prixMin));
        }
        if (prixMax != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("prix"), prixMax));
        }
        if (Boolean.TRUE.equals(certifieeOnly)) {
            spec = spec.and((root, q, cb) -> cb.isTrue(root.get("certifiee")));
        }
        return annonceRepository.findAll(spec, pageable).map(annonceMapper::toListResponse);
    }

    /**
     * Liste paginée des annonces d'un vendeur (toutes ou filtrées par statut).
     * Utilisé dans les onglets « mes annonces » du dashboard.
     */
    @Transactional(readOnly = true)
    public Page<AnnonceListResponse> findByVendeur(Long vendeurId, StatutAnnonce statut, Pageable pageable) {
        Page<Annonce> page = (statut == null)
                ? annonceRepository.findByVendeurId(vendeurId, pageable)
                : annonceRepository.findByVendeurIdAndStatut(vendeurId, statut, pageable);
        return page.map(annonceMapper::toListResponse);
    }

    /**
     * Change le statut d'une annonce (ex. {@code EN_ATTENTE} → {@code ACTIVE},
     * ou {@code ACTIVE} → {@code VENDUE}). Avant tout passage en {@code ACTIVE},
     * vérifie que l'annonce a entre 3 et 5 photos comme exigé par le règlement
     * de la marketplace.
     *
     * @throws BadRequestException si le nombre de photos ne respecte pas la règle
     * @throws AccessDeniedException si l'appelant n'est pas le propriétaire
     */
    @Transactional
    public AnnonceResponse updateStatut(Long vendeurId, Long annonceId, StatutAnnonce nouveauStatut) {
        Annonce annonce = findOwned(vendeurId, annonceId);

        if (nouveauStatut == StatutAnnonce.ACTIVE) {
            long nbPhotos = mediaRepository.countByAnnonceId(annonceId);
            if (nbPhotos < MIN_PHOTOS_REQUISES || nbPhotos > MAX_PHOTOS_AUTORISEES) {
                throw new BadRequestException(
                        "Une annonce doit avoir entre %d et %d photos pour être active (actuellement : %d)"
                                .formatted(MIN_PHOTOS_REQUISES, MAX_PHOTOS_AUTORISEES, nbPhotos));
            }
        }

        annonce.setStatut(nouveauStatut);
        log.info("Statut annonce {} changé en {}", annonceId, nouveauStatut);
        return annonceMapper.toResponse(annonceRepository.save(annonce));
    }

    /**
     * Attache une certification benchmark à une annonce après validation
     * du format de l'URL (domaine dans la liste blanche : Geekbench, 3DMark,
     * CPU-Z…). Le flag {@code certifiee} de l'annonce est positionné à {@code true}.
     *
     * @throws BadRequestException si l'URL fournie ne provient pas d'un site reconnu
     * @throws AccessDeniedException si l'appelant n'est pas le propriétaire
     */
    @Transactional
    public AnnonceResponse submitCertification(Long vendeurId, Long annonceId, CertificationRequest request) {
        Annonce annonce = findOwned(vendeurId, annonceId);
        validateBenchmarkUrl(request.getUrlBenchmark());

        CertificationBenchmark cert = CertificationBenchmark.builder()
                .typeBenchmark(request.getTypeBenchmark())
                .urlBenchmark(request.getUrlBenchmark())
                .scoreMonocoeur(request.getScoreMonocoeur())
                .scoreMulticoeur(request.getScoreMulticoeur())
                .scoreGpu(request.getScoreGpu())
                .logFileUrl(request.getLogFileUrl())
                .logDataJson(request.getLogDataJson())
                .build();

        annonce.attachCertification(cert);
        log.info("Certification {} attachée à l'annonce {}", request.getTypeBenchmark(), annonceId);
        return annonceMapper.toResponse(annonceRepository.save(annonce));
    }

    /**
     * Vérifie qu'une annonce existe et que l'appelant en est bien le propriétaire.
     * Centralise la double vérification {@code 404 + 403} requise par les routes
     * de mutation.
     */
    private Annonce findOwned(Long vendeurId, Long annonceId) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", annonceId));
        if (!annonce.getVendeur().getId().equals(vendeurId)) {
            throw new AccessDeniedException("Vous n'êtes pas le propriétaire de cette annonce");
        }
        return annonce;
    }

    /**
     * Valide que l'URL d'un benchmark provient d'un site officiellement supporté.
     * La liste blanche {@link #BENCHMARK_DOMAINS_AUTORISES} limite les soumissions
     * à des résultats vérifiables (anti-fraude).
     */
    private void validateBenchmarkUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return; // URL optionnelle si l'utilisateur fournit uniquement les logs CPU-Z/HWiNFO
        }
        try {
            String host = java.net.URI.create(url).getHost();
            if (host == null) {
                throw new BadRequestException("URL de benchmark invalide");
            }
            String hostLower = host.toLowerCase();
            boolean ok = BENCHMARK_DOMAINS_AUTORISES.stream().anyMatch(hostLower::endsWith);
            if (!ok) {
                throw new BadRequestException(
                        "L'URL doit provenir d'un site de benchmark reconnu (Geekbench, 3DMark, CPU-Z…)");
            }
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("URL de benchmark invalide : " + ex.getMessage());
        }
    }

    /**
     * Méthode utilitaire (utilisée par les contrôleurs) pour collecter les filtres
     * possibles en une liste lisible — réservée à un usage interne de debug et tests.
     */
    @SuppressWarnings("unused")
    private List<String> describeFilters(CategorieAnnonce categorie, EtatEsthetique etat,
                                         BigDecimal prixMin, BigDecimal prixMax) {
        List<String> parts = new ArrayList<>();
        if (categorie != null) parts.add("categorie=" + categorie);
        if (etat != null) parts.add("etat=" + etat);
        if (prixMin != null) parts.add("prixMin=" + prixMin);
        if (prixMax != null) parts.add("prixMax=" + prixMax);
        return parts;
    }
}
