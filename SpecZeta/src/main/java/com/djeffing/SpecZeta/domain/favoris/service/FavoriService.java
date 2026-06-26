package com.djeffing.SpecZeta.domain.favoris.service;

import com.djeffing.SpecZeta.domain.annonce.entity.Annonce;
import com.djeffing.SpecZeta.domain.annonce.repository.AnnonceRepository;
import com.djeffing.SpecZeta.domain.favoris.dto.FavoriResponse;
import com.djeffing.SpecZeta.domain.favoris.entity.Favori;
import com.djeffing.SpecZeta.domain.favoris.mapper.FavorisMapper;
import com.djeffing.SpecZeta.domain.favoris.repository.FavoriRepository;
import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import com.djeffing.SpecZeta.shared.exception.BadRequestException;
import com.djeffing.SpecZeta.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriService {

    private final FavoriRepository favoriRepository;
    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final FavorisMapper favorisMapper;

    /**
     * Liste paginée des favoris de l'utilisateur courant, transformés en DTO
     * embarquant chaque annonce favorite en projection allégée.
     */
    @Transactional(readOnly = true)
    public Page<FavoriResponse> list(Long userId, Pageable pageable) {
        return favoriRepository.findByUserId(userId, pageable)
                .map(favorisMapper::toResponse);
    }

    /**
     * Ajoute une annonce aux favoris de l'utilisateur.
     * Refuse silencieusement les doublons en renvoyant le favori existant
     * (idempotent : le frontend peut rappeler l'endpoint sans inquiétude).
     * Refuse explicitement quand le vendeur tenterait de favoris-er sa propre annonce.
     *
     * @throws BadRequestException si l'utilisateur favorise sa propre annonce
     * @throws ResourceNotFoundException si l'annonce ou l'utilisateur n'existe pas
     */
    @Transactional
    public FavoriResponse add(Long userId, Long annonceId) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", annonceId));

        if (annonce.getVendeur().getId().equals(userId)) {
            throw new BadRequestException("Vous ne pouvez pas mettre votre propre annonce en favori");
        }

        Favori favori = favoriRepository.findByUserIdAndAnnonceId(userId, annonceId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    Favori created = Favori.builder()
                            .user(user)
                            .annonce(annonce)
                            .build();
                    log.debug("Ajout favori : user={}, annonce={}", userId, annonceId);
                    return favoriRepository.save(created);
                });

        return favorisMapper.toResponse(favori);
    }

    /**
     * Retire un favori. Idempotent : si l'annonce n'est pas dans les favoris,
     * la méthode ne fait rien plutôt que de lever une 404 (cohérent avec un
     * « unfavorite » côté UI où l'état est local et peut désynchroniser).
     */
    @Transactional
    public void remove(Long userId, Long annonceId) {
        favoriRepository.findByUserIdAndAnnonceId(userId, annonceId)
                .ifPresent(favori -> {
                    favoriRepository.delete(favori);
                    log.debug("Retrait favori : user={}, annonce={}", userId, annonceId);
                });
    }

    /**
     * Indique si l'utilisateur a déjà mis cette annonce en favori. Sert au
     * frontend pour afficher l'état du cœur sur la page détail d'une annonce.
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long annonceId) {
        return favoriRepository.existsByUserIdAndAnnonceId(userId, annonceId);
    }
}
