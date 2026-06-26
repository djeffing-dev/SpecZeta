package com.djeffing.SpecZeta.domain.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "CreateConversationRequest",
        description = "Payload de démarrage d'une conversation avec le vendeur d'une annonce. "
                + "Idempotent côté serveur : si une conversation existe déjà pour ce couple "
                + "(acheteur, annonce), elle est réutilisée."
)
public class CreateConversationRequest {

    @NotNull(message = "L'identifiant de l'annonce est requis")
    @Schema(description = "Identifiant de l'annonce concernée.",
            example = "1024",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long annonceId;

    @Size(max = 5000, message = "Le message initial ne peut dépasser 5000 caractères")
    @Schema(description = "Premier message à envoyer en même temps que la création (optionnel).",
            example = "Bonjour, votre PC est-il toujours disponible ?",
            maxLength = 5000,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String messageInitial;
}
