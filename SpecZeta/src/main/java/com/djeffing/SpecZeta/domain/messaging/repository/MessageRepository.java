package com.djeffing.SpecZeta.domain.messaging.repository;

import com.djeffing.SpecZeta.domain.messaging.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Liste paginée des messages d'une conversation, ordonnée par date
     * (utiliser {@code Sort.by("createdAt").ascending()} côté contrôleur).
     */
    Page<Message> findByConversationId(Long conversationId, Pageable pageable);

    /**
     * Retourne le dernier message d'une conversation, utilisé pour l'aperçu
     * dans la liste « mes conversations ».
     */
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);

    /**
     * Compte les messages non lus reçus par un utilisateur dans une conversation.
     * Exclut ses propres messages (un envoi n'est pas une lecture en attente).
     */
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.expediteur.id <> :userId " +
            "AND m.lu = false")
    long countUnreadFor(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * Marque comme lus tous les messages d'une conversation reçus par un utilisateur.
     * Requête bulk pour éviter le chargement individuel de chaque message.
     */
    @Modifying
    @Query("UPDATE Message m SET m.lu = true " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.expediteur.id <> :userId " +
            "AND m.lu = false")
    int markAllAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
