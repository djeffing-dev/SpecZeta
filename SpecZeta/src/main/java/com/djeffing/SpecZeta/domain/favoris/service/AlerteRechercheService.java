package com.djeffing.SpecZeta.domain.favoris.service;

import com.djeffing.SpecZeta.domain.favoris.dto.AlerteResponse;
import com.djeffing.SpecZeta.domain.favoris.dto.CreateAlerteRequest;
import com.djeffing.SpecZeta.domain.favoris.dto.UpdateAlerteRequest;
import com.djeffing.SpecZeta.domain.favoris.entity.AlerteRecherche;
import com.djeffing.SpecZeta.domain.favoris.mapper.FavorisMapper;
import com.djeffing.SpecZeta.domain.favoris.repository.AlerteRechercheRepository;
import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import com.djeffing.SpecZeta.shared.exception.BadRequestException;
import com.djeffing.SpecZeta.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlerteRechercheService {

    private final AlerteRechercheRepository alerteRepository;
    private final UserRepository userRepository;
    private final FavorisMapper favorisMapper;

    /**
     * Liste paginée des alertes de recherche de l'utilisateur courant
     * (actives ET inactives confondues). Le tri est piloté par le {@code Pageable}.
     */
    @Transactional(readOnly = true)
    public Page<AlerteResponse> list(Long userId, Pageable pageable) {
        return alerteRepository.findByUserId(userId, pageable)
                .map(favorisMapper::toAlerteResponse);
    }

    /**
     * Crée une nouvelle alerte pour l'utilisateur courant. Valide la cohérence
     * de la fourchette de prix avant la persistance (prixMin ≤ prixMax).
     *
     * @throws BadRequestException si {@code prixMin > prixMax}
     */
    @Transactional
    public AlerteResponse create(Long userId, CreateAlerteRequest request) {
        validatePriceRange(request.getPrixMin(), request.getPrixMax());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        AlerteRecherche alerte = AlerteRecherche.builder()
                .user(user)
                .motsCles(request.getMotsCles())
                .prixMin(request.getPrixMin())
                .prixMax(request.getPrixMax())
                .categorie(request.getCategorie())
                .localisation(request.getLocalisation())
                .rayonKm(request.getRayonKm())
                .active(Boolean.TRUE.equals(request.getActive()))
                .build();

        AlerteRecherche saved = alerteRepository.save(alerte);
        log.info("Alerte créée : id={}, user={}", saved.getId(), userId);
        return favorisMapper.toAlerteResponse(saved);
    }

    /**
     * Met à jour une alerte existante (PATCH partiel applicatif).
     * Vérifie que l'alerte appartient bien à l'utilisateur courant.
     *
     * @throws AccessDeniedException si l'utilisateur tente de modifier l'alerte d'autrui
     */
    @Transactional
    public AlerteResponse update(Long userId, Long alerteId, UpdateAlerteRequest request) {
        AlerteRecherche alerte = findOwned(userId, alerteId);

        if (request.getMotsCles() != null) alerte.setMotsCles(request.getMotsCles());
        if (request.getPrixMin() != null) alerte.setPrixMin(request.getPrixMin());
        if (request.getPrixMax() != null) alerte.setPrixMax(request.getPrixMax());
        if (request.getCategorie() != null) alerte.setCategorie(request.getCategorie());
        if (request.getLocalisation() != null) alerte.setLocalisation(request.getLocalisation());
        if (request.getRayonKm() != null) alerte.setRayonKm(request.getRayonKm());
        if (request.getActive() != null) alerte.setActive(request.getActive());

        validatePriceRange(alerte.getPrixMin(), alerte.getPrixMax());

        return favorisMapper.toAlerteResponse(alerteRepository.save(alerte));
    }

    /**
     * Supprime définitivement une alerte. Vérifie la propriété avant suppression.
     */
    @Transactional
    public void delete(Long userId, Long alerteId) {
        AlerteRecherche alerte = findOwned(userId, alerteId);
        alerteRepository.delete(alerte);
        log.info("Alerte supprimée : id={}, user={}", alerteId, userId);
    }

    /**
     * Récupère l'alerte si elle existe ET appartient à l'utilisateur,
     * sinon lève {@code 404} ou {@code 403} selon le cas.
     * Centralise la double vérification utilisée par update et delete.
     */
    private AlerteRecherche findOwned(Long userId, Long alerteId) {
        AlerteRecherche alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte", "id", alerteId));
        if (!alerte.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Vous n'êtes pas propriétaire de cette alerte");
        }
        return alerte;
    }

    /**
     * Vérifie que la fourchette de prix est cohérente.
     * Tolère les valeurs nulles (filtre optionnel) ; n'échoue que si les deux
     * sont définis et que {@code min > max}.
     */
    private void validatePriceRange(BigDecimal prixMin, BigDecimal prixMax) {
        if (prixMin != null && prixMax != null && prixMin.compareTo(prixMax) > 0) {
            throw new BadRequestException("Le prix minimum ne peut être supérieur au prix maximum");
        }
    }
}
