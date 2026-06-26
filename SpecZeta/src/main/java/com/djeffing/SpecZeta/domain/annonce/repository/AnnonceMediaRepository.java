package com.djeffing.SpecZeta.domain.annonce.repository;

import com.djeffing.SpecZeta.domain.annonce.entity.AnnonceMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnonceMediaRepository extends JpaRepository<AnnonceMedia, Long> {

    /**
     * Retourne les médias d'une annonce ordonnés par champ {@code ordre} croissant.
     * Permet d'accéder à la photo principale (index 0) sans charger l'annonce entière.
     */
    List<AnnonceMedia> findByAnnonceIdOrderByOrdreAsc(Long annonceId);

    /**
     * Compte le nombre de photos attachées à une annonce.
     * Utilisé par le service pour valider la règle « 3 à 5 photos » avant
     * d'autoriser le passage en statut ACTIVE.
     */
    long countByAnnonceId(Long annonceId);

    /**
     * Supprime en bloc tous les médias d'une annonce. Spring Data dérive un
     * DELETE depuis le nom de méthode ; nécessite {@code @Transactional} côté
     * appelant. Utilisé pour remplacer les photos d'une annonce avant un nouvel
     * upload (3-5 nouvelles photos).
     */
    @org.springframework.transaction.annotation.Transactional
    void deleteByAnnonceId(Long annonceId);
}
