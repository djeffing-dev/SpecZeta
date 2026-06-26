package com.djeffing.SpecZeta.domain.user.repository;

import com.djeffing.SpecZeta.domain.user.entity.UserRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRatingRepository extends JpaRepository<UserRating, Long> {

    /**
     * Liste paginée des évaluations reçues par un utilisateur,
     * triée par {@code Pageable} (typiquement createdAt DESC côté contrôleur).
     */
    Page<UserRating> findByEvalueId(Long evalueId, Pageable pageable);

    /**
     * Vérifie si un évaluateur a déjà noté une annonce précise (utilisé pour
     * empêcher les évaluations multiples pour un même achat).
     */
    Optional<UserRating> findByEvaluateurIdAndAnnonceId(Long evaluateurId, Long annonceId);

    /**
     * Calcule la moyenne arrondie des notes reçues par un utilisateur.
     * Renvoie {@code 0.0} si aucune évaluation n'existe (COALESCE).
     */
    @Query("SELECT COALESCE(AVG(r.note), 0.0) FROM UserRating r WHERE r.evalue.id = :evalueId")
    Double averageNoteByEvalueId(@Param("evalueId") Long evalueId);

    /**
     * Compte le nombre total d'évaluations reçues. Utilisé pour mettre à jour
     * le champ dénormalisé {@code User.nombreEvaluations}.
     */
    long countByEvalueId(Long evalueId);
}
