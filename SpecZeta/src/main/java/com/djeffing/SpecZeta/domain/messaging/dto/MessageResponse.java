package com.djeffing.SpecZeta.domain.messaging.dto;

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
        name = "MessageResponse",
        description = "Vue lecture d'un message d'une conversation."
)
public class MessageResponse {

    @Schema(description = "Identifiant unique du message.", example = "9012")
    private Long id;

    @Schema(description = "Identifiant de la conversation à laquelle le message appartient.", example = "57")
    private Long conversationId;

    @Schema(description = "Identifiant de l'expéditeur.", example = "42")
    private Long expediteurId;

    @Schema(description = "Pseudo de l'expéditeur (affichage rapide).", example = "JaneD")
    private String expediteurPseudo;

    @Schema(description = "Contenu textuel.",
            example = "Le PC est encore disponible.")
    private String contenu;

    @Schema(description = "`true` si le destinataire a marqué le message comme lu.", example = "false")
    private Boolean lu;

    @Schema(description = "Horodatage d'envoi.", example = "2026-06-03T10:24:17")
    private LocalDateTime createdAt;
}
