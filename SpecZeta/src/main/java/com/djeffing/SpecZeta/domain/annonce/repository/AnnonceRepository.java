package com.djeffing.SpecZeta.domain.annonce.repository;

import com.djeffing.SpecZeta.domain.annonce.entity.Annonce;
import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import com.djeffing.SpecZeta.domain.annonce.enums.StatutAnnonce;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {

    /**
     * Liste paginée des annonces d'un vendeur, tous statuts confondus.
     * Utilisé sur la page « mes annonces » du dashboard.
     */
    Page<Annonce> findByVendeurId(Long vendeurId, Pageable pageable);

    /**
     * Liste paginée filtrée par statut pour le tableau de bord vendeur
     * (onglets « actives », « vendues », etc.).
     */
    Page<Annonce> findByVendeurIdAndStatut(Long vendeurId, StatutAnnonce statut, Pageable pageable);

    /**
     * Compte d'annonces d'un vendeur par statut. Utilisé pour les badges
     * « 3 actives / 1 vendue / 2 en attente » du dashboard.
     */
    long countByVendeurIdAndStatut(Long vendeurId, StatutAnnonce statut);

    /**
     * Somme des prix des annonces vendues d'un vendeur. Le {@code COALESCE}
     * garantit le retour de zéro plutôt que {@code null} pour un vendeur sans vente.
     */
    @Query("SELECT COALESCE(SUM(a.prix), 0) FROM Annonce a " +
            "WHERE a.vendeur.id = :vendeurId AND a.statut = :statut")
    BigDecimal sumPrixByVendeurAndStatut(@Param("vendeurId") Long vendeurId,
                                        @Param("statut") StatutAnnonce statut);

    /**
     * Liste paginée des annonces actives d'une catégorie pour le feed public,
     * trié selon le {@code Pageable} fourni (date, prix, etc.).
     */
    Page<Annonce> findByStatutAndCategorie(StatutAnnonce statut, CategorieAnnonce categorie, Pageable pageable);
}
