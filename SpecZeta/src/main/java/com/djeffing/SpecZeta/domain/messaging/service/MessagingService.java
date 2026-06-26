package com.djeffing.SpecZeta.domain.messaging.service;

import com.djeffing.SpecZeta.domain.annonce.entity.Annonce;
import com.djeffing.SpecZeta.domain.annonce.repository.AnnonceRepository;
import com.djeffing.SpecZeta.domain.messaging.dto.ConversationResponse;
import com.djeffing.SpecZeta.domain.messaging.dto.CreateConversationRequest;
import com.djeffing.SpecZeta.domain.messaging.dto.CreateMessageRequest;
import com.djeffing.SpecZeta.domain.messaging.dto.MessageResponse;
import com.djeffing.SpecZeta.domain.messaging.entity.Conversation;
import com.djeffing.SpecZeta.domain.messaging.entity.Message;
import com.djeffing.SpecZeta.domain.messaging.mapper.MessagingMapper;
import com.djeffing.SpecZeta.domain.messaging.repository.ConversationRepository;
import com.djeffing.SpecZeta.domain.messaging.repository.MessageRepository;
import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.mapper.UserMapper;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import com.djeffing.SpecZeta.shared.exception.BadRequestException;
import com.djeffing.SpecZeta.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagingService {

    private static final String STOMP_USER_DESTINATION = "/queue/messages";

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final MessagingMapper messagingMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Démarre une conversation autour d'une annonce. Si une conversation existe
     * déjà entre cet acheteur et cette annonce, elle est réutilisée
     * (évite les doublons). Si un message initial est fourni, il est envoyé
     * immédiatement et poussé en temps réel au vendeur via WebSocket.
     *
     * @throws BadRequestException si l'utilisateur tente de se contacter lui-même
     */
    @Transactional
    public ConversationResponse findOrCreate(Long userId, CreateConversationRequest request) {
        Annonce annonce = annonceRepository.findById(request.getAnnonceId())
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", request.getAnnonceId()));

        if (annonce.getVendeur().getId().equals(userId)) {
            throw new BadRequestException("Vous ne pouvez pas démarrer une conversation sur votre propre annonce");
        }

        User acheteur = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Conversation conversation = conversationRepository
                .findByAnnonceIdAndAcheteurId(annonce.getId(), userId)
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .annonce(annonce)
                        .acheteur(acheteur)
                        .vendeur(annonce.getVendeur())
                        .build()));

        if (StringUtils.hasText(request.getMessageInitial())) {
            sendMessage(userId, conversation.getId(),
                    CreateMessageRequest.builder().contenu(request.getMessageInitial()).build());
        }

        return buildConversationResponse(conversation, userId);
    }

    /**
     * Liste paginée des conversations de l'utilisateur (en tant qu'acheteur OU vendeur).
     * Chaque entrée embarque le dernier message et le nombre de messages non lus
     * adressés à l'utilisateur pour faciliter l'affichage côté frontend.
     */
    @Transactional(readOnly = true)
    public Page<ConversationResponse> listForUser(Long userId, Pageable pageable) {
        return conversationRepository.findAllForUser(userId, pageable)
                .map(c -> buildConversationResponse(c, userId));
    }

    /**
     * Liste paginée des messages d'une conversation. Vérifie d'abord que
     * l'utilisateur est bien participant (acheteur ou vendeur) ; refuse l'accès sinon.
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long userId, Long conversationId, Pageable pageable) {
        ensureParticipant(userId, conversationId);
        return messageRepository.findByConversationId(conversationId, pageable)
                .map(messagingMapper::toResponse);
    }

    /**
     * Envoie un message dans une conversation. Met à jour {@code lastMessageAt}
     * sur la conversation et pousse le message en temps réel au destinataire
     * via STOMP sur la destination {@code /user/{destinataireId}/queue/messages}.
     *
     * @throws AccessDeniedException si l'utilisateur n'est pas participant
     */
    @Transactional
    public MessageResponse sendMessage(Long userId, Long conversationId, CreateMessageRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        ensureParticipant(userId, conversation);

        User expediteur = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Message message = Message.builder()
                .conversation(conversation)
                .expediteur(expediteur)
                .contenu(request.getContenu())
                .lu(false)
                .build();

        conversation.addMessage(message);
        Message saved = messageRepository.save(message);
        conversationRepository.save(conversation);

        Long destinataireId = userId.equals(conversation.getAcheteur().getId())
                ? conversation.getVendeur().getId()
                : conversation.getAcheteur().getId();

        MessageResponse response = messagingMapper.toResponse(saved);
        pushToUser(destinataireId, response);

        log.debug("Message envoyé : conv={}, from={}, to={}", conversationId, userId, destinataireId);
        return response;
    }

    /**
     * Marque comme lus tous les messages reçus par l'utilisateur dans une conversation.
     * Utilisé quand l'utilisateur ouvre la fenêtre de discussion côté frontend.
     */
    @Transactional
    public int markAsRead(Long userId, Long conversationId) {
        ensureParticipant(userId, conversationId);
        return messageRepository.markAllAsRead(conversationId, userId);
    }

    /**
     * Pousse un message JSON à l'utilisateur cible via STOMP sur sa file dédiée.
     * Spring résout {@code "/user/{id}/queue/messages"} automatiquement à partir
     * de l'identité passée en premier argument.
     */
    private void pushToUser(Long destinataireId, MessageResponse message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(destinataireId),
                    STOMP_USER_DESTINATION,
                    message);
        } catch (Exception ex) {
            log.warn("Échec push STOMP pour user {} : {}", destinataireId, ex.getMessage());
        }
    }

    /**
     * Vérifie qu'un utilisateur est participant d'une conversation à partir de son id.
     * Charge la conversation puis délègue à la variante avec entité.
     */
    private void ensureParticipant(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        ensureParticipant(userId, conversation);
    }

    /**
     * Vérifie qu'un utilisateur est acheteur ou vendeur de la conversation fournie.
     *
     * @throws AccessDeniedException si l'utilisateur n'a aucun lien avec la conversation
     */
    private void ensureParticipant(Long userId, Conversation conversation) {
        boolean isMember = conversation.getAcheteur().getId().equals(userId)
                || conversation.getVendeur().getId().equals(userId);
        if (!isMember) {
            throw new AccessDeniedException("Vous n'êtes pas participant à cette conversation");
        }
    }

    /**
     * Construit la {@link ConversationResponse} complète à destination du client :
     * détails de l'annonce, profils résumés des deux participants, dernier message
     * et badge de messages non lus calculé pour l'utilisateur appelant.
     */
    private ConversationResponse buildConversationResponse(Conversation conversation, Long userId) {
        Annonce annonce = conversation.getAnnonce();
        String photoUrl = (annonce.getMedias() != null && !annonce.getMedias().isEmpty())
                ? annonce.getMedias().get(0).getDropboxUrl()
                : null;

        MessageResponse dernier = messageRepository
                .findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .map(messagingMapper::toResponse)
                .orElse(null);

        long nonLus = messageRepository.countUnreadFor(conversation.getId(), userId);

        return ConversationResponse.builder()
                .id(conversation.getId())
                .annonceId(annonce.getId())
                .annonceTitre(annonce.getTitre())
                .annoncePhotoUrl(photoUrl)
                .acheteur(userMapper.toSummary(conversation.getAcheteur()))
                .vendeur(userMapper.toSummary(conversation.getVendeur()))
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .dernierMessage(dernier)
                .messagesNonLus(nonLus)
                .build();
    }
}
