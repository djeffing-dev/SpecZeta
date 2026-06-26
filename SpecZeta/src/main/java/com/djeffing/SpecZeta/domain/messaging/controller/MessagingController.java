package com.djeffing.SpecZeta.domain.messaging.controller;

import com.djeffing.SpecZeta.domain.messaging.dto.ConversationResponse;
import com.djeffing.SpecZeta.domain.messaging.dto.CreateConversationRequest;
import com.djeffing.SpecZeta.domain.messaging.dto.CreateMessageRequest;
import com.djeffing.SpecZeta.domain.messaging.dto.MessageResponse;
import com.djeffing.SpecZeta.domain.messaging.service.MessagingService;
import com.djeffing.SpecZeta.security.SecurityUtils;
import com.djeffing.SpecZeta.shared.dto.ApiResponse;
import com.djeffing.SpecZeta.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Messagerie",
        description = "Conversations et messages entre acheteurs et vendeurs autour d'une annonce. "
                + "Les nouveaux messages sont également diffusés en temps réel via WebSocket STOMP "
                + "sur la destination `/user/{id}/queue/messages`.")
@SecurityRequirement(name = "bearerAuth")
public class MessagingController {

    private final MessagingService messagingService;

    @Operation(
            summary = "Lister les conversations de l'utilisateur connecté",
            description = """
                    Retourne une page paginée des conversations dans lesquelles l'utilisateur
                    courant intervient en tant qu'acheteur ou vendeur. Tri par défaut sur
                    `lastMessageAt` décroissant (conversations actives en premier).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page de conversations."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ConversationResponse>>> myConversations(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "lastMessageAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<ConversationResponse> page = messagingService.listForUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(page)));
    }

    @Operation(
            summary = "Démarrer (ou récupérer) une conversation avec le vendeur d'une annonce",
            description = """
                    Crée une conversation entre l'utilisateur connecté et le vendeur de l'annonce
                    ciblée. Si une conversation existe déjà pour ce couple (acheteur, annonce),
                    elle est renvoyée telle quelle (idempotent).

                    Un message initial peut être envoyé directement via le champ `messageInitial`.

                    **Règle métier :** un vendeur ne peut pas démarrer une conversation sur sa
                    propre annonce.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Conversation créée ou réutilisée."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payload invalide ou conversation avec soi-même."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce introuvable.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> startConversation(
            @Valid @RequestBody CreateConversationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ConversationResponse response = messagingService.findOrCreate(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Conversation prête"));
    }

    @Operation(
            summary = "Lister les messages d'une conversation",
            description = """
                    Retourne la liste paginée des messages échangés dans la conversation.
                    Tri par défaut sur `createdAt` croissant (ordre chronologique de lecture).

                    L'accès est refusé (`403`) si l'utilisateur n'est ni acheteur ni vendeur
                    de la conversation.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page de messages."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "L'utilisateur n'appartient pas à cette conversation."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation introuvable.")
    })
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getMessages(
            @Parameter(description = "Identifiant de la conversation.", example = "57")
            @PathVariable Long conversationId,
            @Parameter(hidden = true)
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<MessageResponse> page = messagingService.getMessages(userId, conversationId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(page)));
    }

    @Operation(
            summary = "Envoyer un message dans une conversation",
            description = """
                    Persiste un nouveau message dans la conversation, met à jour le champ
                    `lastMessageAt` et pousse le message au destinataire via WebSocket sur
                    `/user/{destinataireId}/queue/messages`.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Message envoyé et propagé."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Contenu vide ou trop long."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "L'utilisateur n'appartient pas à cette conversation."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation introuvable.")
    })
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Parameter(description = "Identifiant de la conversation.", example = "57")
            @PathVariable Long conversationId,
            @Valid @RequestBody CreateMessageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        MessageResponse message = messagingService.sendMessage(userId, conversationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(message));
    }

    @Operation(
            summary = "Marquer comme lus tous les messages d'une conversation",
            description = """
                    Passe à l'état `lu = true` tous les messages reçus par l'utilisateur dans
                    la conversation. Retourne le nombre exact de messages effectivement modifiés
                    (utile pour mettre à jour les badges de notifications côté frontend).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Messages marqués comme lus."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT manquant ou expiré."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "L'utilisateur n'appartient pas à cette conversation."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation introuvable.")
    })
    @PatchMapping("/{conversationId}/read")
    public ResponseEntity<ApiResponse<Integer>> markAsRead(
            @Parameter(description = "Identifiant de la conversation.", example = "57")
            @PathVariable Long conversationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        int updated = messagingService.markAsRead(userId, conversationId);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Messages marqués comme lus"));
    }
}
