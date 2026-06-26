package com.djeffing.SpecZeta.domain.favoris.repository;

import com.djeffing.SpecZeta.domain.favoris.entity.Favori;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, Long> {

    /**
     * Liste paginée des favoris de l'utilisateur, ordonnée par le {@code Pageable}
     * (typiquement {@code createdAt DESC} pour afficher les ajouts récents en tête).
     */
    Page<Favori> findByUserId(Long userId, Pageable pageable);

    /**
     * Recherche un favori précis par couple (utilisateur, annonce).
     * Utilisé pour la suppression depuis l'endpoint {@code DELETE /favoris/{annonceId}}
     * et pour vérifier l'absence de doublon avant ajout.
     */
    Optional<Favori> findByUserIdAndAnnonceId(Long userId, Long annonceId);

    /**
     * Vérifie qu'un utilisateur a déjà mis en favori une annonce, sans charger l'entité.
     * Plus rapide qu'un {@code findByUserIdAndAnnonceId().isPresent()}.
     */
    boolean existsByUserIdAndAnnonceId(Long userId, Long annonceId);

    /**
     * Compte le nombre total d'utilisateurs ayant favoris-é une annonce.
     * Utilisé pour la statistique « favoris reçus » du dashboard vendeur.
     */
    long countByAnnonceId(Long annonceId);

    /**
     * Compte les favoris totaux reçus par toutes les annonces d'un vendeur.
     * Requête agrégée pour le badge dashboard sans charger les listes.
     */
    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(f) FROM Favori f WHERE f.annonce.vendeur.id = :vendeurId")
    long countForVendeur(@org.springframework.data.repository.query.Param("vendeurId") Long vendeurId);
}
