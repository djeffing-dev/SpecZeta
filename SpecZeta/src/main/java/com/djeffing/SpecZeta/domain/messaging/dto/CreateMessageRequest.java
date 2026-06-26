package com.djeffing.SpecZeta.domain.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
        name = "CreateMessageRequest",
        description = "Payload d'envoi d'un message dans une conversation existante."
)
public class CreateMessageRequest {

    @NotBlank(message = "Le contenu du message ne peut pas être vide")
    @Size(max = 5000, message = "Le message ne peut dépasser 5000 caractères")
    @Schema(description = "Contenu textuel du message.",
            example = "Le PC est encore disponible. Je peux vous l'envoyer en Colissimo demain.",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 5000)
    private String contenu;
}
