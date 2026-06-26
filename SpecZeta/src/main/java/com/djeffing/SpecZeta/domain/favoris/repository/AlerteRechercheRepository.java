package com.djeffing.SpecZeta.domain.favoris.repository;

import com.djeffing.SpecZeta.domain.favoris.entity.AlerteRecherche;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlerteRechercheRepository extends JpaRepository<AlerteRecherche, Long> {

    /**
     * Liste paginée des alertes d'un utilisateur, tous statuts confondus.
     * Utilisé pour la page « mes alertes » côté frontend.
     */
    Page<AlerteRecherche> findByUserId(Long userId, Pageable pageable);

    /**
     * Toutes les alertes actives du système, exploitée par un éventuel scheduler
     * de matching d'annonces (« telle annonce nouvelle correspond à telle alerte »).
     * Pas de pagination car ce traitement est batch en arrière-plan.
     */
    List<AlerteRecherche> findByActiveTrue();

    /**
     * Compte les alertes actives d'un utilisateur, utilisé dans la vue profil
     * pour afficher un compteur résumé.
     */
    long countByUserIdAndActiveTrue(Long userId);
}
