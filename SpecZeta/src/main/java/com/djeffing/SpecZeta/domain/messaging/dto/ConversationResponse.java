package com.djeffing.SpecZeta.domain.messaging.dto;

import com.djeffing.SpecZeta.domain.user.dto.UserSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "ConversationResponse",
        description = "Vue lecture d'une conversation acheteur ↔ vendeur autour d'une annonce."
)
public class ConversationResponse {

    @Schema(description = "Identifiant unique de la conversation.", example = "57")
    private Long id;

    @Schema(description = "Identifiant de l'annonce concernée.", example = "1024")
    private Long annonceId;

    @Schema(description = "Titre de l'annonce (affichage rapide).",
            example = "PC Portable Dell XPS 15 (2023) — Core i7")
    private String annonceTitre;

    @Schema(description = "URL de la photo principale de l'annonce.",
            example = "https://dl.dropboxusercontent.com/s/abc/annonce-1024-1.jpg",
            format = "uri")
    private String annoncePhotoUrl;

    @Schema(description = "Profil résumé de l'acheteur.")
    private UserSummaryResponse acheteur;

    @Schema(description = "Profil résumé du vendeur.")
    private UserSummaryResponse vendeur;

    @Schema(description = "Horodatage du dernier message échangé.", example = "2026-06-03T10:24:17")
    private LocalDateTime lastMessageAt;

    @Schema(description = "Horodatage de création de la conversation.", example = "2026-06-01T08:12:03")
    private LocalDateTime createdAt;

    @Schema(description = "Dernier message échangé (peut être `null` si aucun message n'a encore été envoyé).")
    private MessageResponse dernierMessage;

    @Schema(description = "Nombre de messages non lus pour l'utilisateur courant dans cette conversation.",
            example = "2")
    private long messagesNonLus;
}
