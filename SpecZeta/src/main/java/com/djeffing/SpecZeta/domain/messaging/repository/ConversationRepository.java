package com.djeffing.SpecZeta.domain.messaging.repository;

import com.djeffing.SpecZeta.domain.messaging.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Vérifie l'existence d'une conversation entre un acheteur et le vendeur
     * pour une annonce donnée. La contrainte unique en base interdit déjà
     * les doublons ; ce findByXxx permet de réutiliser une conversation existante.
     */
    Optional<Conversation> findByAnnonceIdAndAcheteurId(Long annonceId, Long acheteurId);

    /**
     * Liste paginée des conversations dans lesquelles l'utilisateur est impliqué
     * (en tant qu'acheteur OU vendeur). Trié par dernier message (le plus récent
     * en tête) via le {@code Pageable} fourni côté contrôleur.
     */
    @Query("SELECT c FROM Conversation c " +
            "WHERE c.acheteur.id = :userId OR c.vendeur.id = :userId")
    Page<Conversation> findAllForUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Compte les conversations contenant au moins un message non lu adressé à
     * l'utilisateur (i.e. dont il n'est PAS l'expéditeur). Utilisé pour le badge
     * « messages non lus » dans le dashboard.
     */
    @Query("SELECT COUNT(DISTINCT c.id) FROM Conversation c " +
            "JOIN c.messages m " +
            "WHERE (c.acheteur.id = :userId OR c.vendeur.id = :userId) " +
            "AND m.expediteur.id <> :userId " +
            "AND m.lu = false")
    long countConversationsWithUnreadMessages(@Param("userId") Long userId);
}
