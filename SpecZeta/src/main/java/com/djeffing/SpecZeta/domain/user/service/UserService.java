package com.djeffing.SpecZeta.domain.user.service;

import com.djeffing.SpecZeta.domain.annonce.enums.StatutAnnonce;
import com.djeffing.SpecZeta.domain.annonce.repository.AnnonceRepository;
import com.djeffing.SpecZeta.domain.favoris.repository.FavoriRepository;
import com.djeffing.SpecZeta.domain.messaging.repository.ConversationRepository;
import com.djeffing.SpecZeta.domain.user.dto.DashboardResponse;
import com.djeffing.SpecZeta.domain.user.dto.RatingRequest;
import com.djeffing.SpecZeta.domain.user.dto.RatingResponse;
import com.djeffing.SpecZeta.domain.user.dto.UpdateProfileRequest;
import com.djeffing.SpecZeta.domain.user.dto.UserProfileResponse;
import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.entity.UserProfile;
import com.djeffing.SpecZeta.domain.user.entity.UserRating;
import com.djeffing.SpecZeta.domain.user.mapper.UserMapper;
import com.djeffing.SpecZeta.domain.user.repository.UserProfileRepository;
import com.djeffing.SpecZeta.domain.user.repository.UserRatingRepository;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import com.djeffing.SpecZeta.shared.exception.BadRequestException;
import com.djeffing.SpecZeta.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserRatingRepository ratingRepository;
    private final AnnonceRepository annonceRepository;
    private final FavoriRepository favoriRepository;
    private final ConversationRepository conversationRepository;
    private final UserMapper userMapper;

    /**
     * Récupère le profil complet de l'utilisateur courant (vue privée :
     * inclut l'email, le téléphone, l'adresse complète).
     * Crée le profil étendu vide si l'utilisateur n'en a jamais renseigné.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = findUser(userId);
        UserProfile profile = profileRepository.findByUserId(userId).orElse(null);
        return userMapper.toProfileResponse(user, profile);
    }

    /**
     * Récupère la vue publique du profil d'un utilisateur (vendeur typiquement).
     * Retourne les mêmes champs que le profil privé — la non-exposition de l'email/téléphone
     * est gérée côté contrôleur si nécessaire.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfile(Long userId) {
        User user = findUser(userId);
        UserProfile profile = profileRepository.findByUserId(userId).orElse(null);
        return userMapper.toProfileResponse(user, profile);
    }

    /**
     * Met à jour le profil de l'utilisateur courant. Applique les champs reçus
     * uniquement s'ils sont non-null (PATCH partiel), et provisionne le
     * {@link UserProfile} étendu à la demande.
     *
     * @throws BadRequestException si le pseudo demandé est déjà pris
     */
    @Transactional
    public UserProfileResponse updateMyProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);

        if (StringUtils.hasText(request.getPseudo()) && !request.getPseudo().equals(user.getPseudo())) {
            if (userRepository.existsByPseudo(request.getPseudo())) {
                throw new BadRequestException("Ce pseudo est déjà pris");
            }
            user.setPseudo(request.getPseudo());
        }
        if (request.getVille() != null) user.setVille(request.getVille());
        if (request.getTelephone() != null) user.setTelephone(request.getTelephone());
        if (request.getPhotoUrl() != null) user.setPhotoUrl(request.getPhotoUrl());

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().user(user).build());

        if (request.getBiographie() != null) profile.setBiographie(request.getBiographie());
        if (request.getDateNaissance() != null) profile.setDateNaissance(request.getDateNaissance());
        if (request.getAdresse() != null) profile.setAdresse(request.getAdresse());
        if (request.getCodePostal() != null) profile.setCodePostal(request.getCodePostal());
        if (request.getPays() != null) profile.setPays(request.getPays());
        if (request.getSiteWeb() != null) profile.setSiteWeb(request.getSiteWeb());

        profileRepository.save(profile);
        User saved = userRepository.save(user);

        log.debug("Profil mis à jour : userId={}", userId);
        return userMapper.toProfileResponse(saved, profile);
    }

    /**
     * Construit le tableau de bord vendeur : compteurs d'annonces par statut,
     * revenu total des annonces vendues, statistiques de notation, et — depuis
     * l'Étape 4 — nombre de conversations avec messages non lus et nombre total
     * de favoris reçus sur l'ensemble des annonces du vendeur.
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        User user = findUser(userId);

        long actives = annonceRepository.countByVendeurIdAndStatut(userId, StatutAnnonce.ACTIVE);
        long vendues = annonceRepository.countByVendeurIdAndStatut(userId, StatutAnnonce.VENDUE);
        long enAttente = annonceRepository.countByVendeurIdAndStatut(userId, StatutAnnonce.EN_ATTENTE);
        long suspendues = annonceRepository.countByVendeurIdAndStatut(userId, StatutAnnonce.SUSPENDUE);
        BigDecimal revenu = annonceRepository.sumPrixByVendeurAndStatut(userId, StatutAnnonce.VENDUE);

        long convNonLues = conversationRepository.countConversationsWithUnreadMessages(userId);
        long favRecus = favoriRepository.countForVendeur(userId);

        return DashboardResponse.builder()
                .annoncesActives(actives)
                .annoncesVendues(vendues)
                .annoncesEnAttente(enAttente)
                .annoncesSuspendues(suspendues)
                .revenuTotal(revenu != null ? revenu : BigDecimal.ZERO)
                .conversationsNonLues(convNonLues)
                .favorisRecus(favRecus)
                .ratingMoyenne(user.getRatingMoyenne())
                .nombreEvaluations(user.getNombreEvaluations())
                .build();
    }

    /**
     * Enregistre une nouvelle évaluation reçue par un utilisateur.
     * Refuse l'auto-évaluation et empêche un même évaluateur de noter deux fois
     * la même annonce. Met à jour les compteurs dénormalisés ({@code ratingMoyenne}
     * et {@code nombreEvaluations}) sur l'évalué pour éviter une agrégation à chaque lecture.
     *
     * @throws BadRequestException si l'utilisateur tente de s'auto-noter ou de doublonner
     */
    @Transactional
    public RatingResponse submitRating(Long evaluateurId, Long evalueId, RatingRequest request) {
        if (evaluateurId.equals(evalueId)) {
            throw new BadRequestException("Vous ne pouvez pas vous évaluer vous-même");
        }

        User evaluateur = findUser(evaluateurId);
        User evalue = findUser(evalueId);

        if (request.getAnnonceId() != null) {
            ratingRepository.findByEvaluateurIdAndAnnonceId(evaluateurId, request.getAnnonceId())
                    .ifPresent(r -> {
                        throw new BadRequestException("Vous avez déjà évalué cette annonce");
                    });
        }

        UserRating rating = UserRating.builder()
                .evalue(evalue)
                .evaluateur(evaluateur)
                .note(request.getNote())
                .commentaire(request.getCommentaire())
                .build();

        if (request.getAnnonceId() != null) {
            // L'association à l'annonce est faite par référence légère pour économiser un SELECT.
            // Si l'annonce n'existe pas, la contrainte FK lèvera une DataIntegrityViolationException.
            com.djeffing.SpecZeta.domain.annonce.entity.Annonce ann =
                    annonceRepository.findById(request.getAnnonceId())
                            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", request.getAnnonceId()));
            rating.setAnnonce(ann);
        }

        UserRating saved = ratingRepository.save(rating);
        recomputeAverage(evalue);

        return RatingResponse.builder()
                .id(saved.getId())
                .evalueId(evalueId)
                .evaluateurId(evaluateurId)
                .evaluateurPseudo(evaluateur.getPseudo())
                .annonceId(request.getAnnonceId())
                .note(saved.getNote())
                .commentaire(saved.getCommentaire())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * Recalcule la moyenne et le nombre d'évaluations d'un utilisateur après
     * l'ajout d'une note. Les valeurs sont stockées sur {@link User} pour des
     * lectures rapides dans les listes d'annonces (pas d'agrégation à la volée).
     */
    private void recomputeAverage(User evalue) {
        Double avg = ratingRepository.averageNoteByEvalueId(evalue.getId());
        long count = ratingRepository.countByEvalueId(evalue.getId());
        evalue.setRatingMoyenne(avg != null ? avg : 0.0);
        evalue.setNombreEvaluations((int) count);
        userRepository.save(evalue);
    }

    /**
     * Charge un {@link User} par id ou lance une exception 404.
     * Centralise la même requête pour toutes les méthodes du service.
     */
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
