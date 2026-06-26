package com.djeffing.SpecZeta.domain.favoris.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "AddFavoriRequest",
        description = "Payload d'ajout d'une annonce aux favoris."
)
public class AddFavoriRequest {

    @NotNull(message = "L'identifiant de l'annonce est requis")
    @Schema(description = "Identifiant de l'annonce à ajouter aux favoris.",
            example = "1024",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long annonceId;
}
