package com.djeffing.SpecZeta.domain.annonce.dto;

import com.djeffing.SpecZeta.domain.annonce.enums.StatutAnnonce;
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
        name = "UpdateStatutRequest",
        description = "Payload de changement de statut d'une annonce. Le service vérifie la "
                + "légitimité de la transition (ex. interdit le passage en ACTIVE sans 3-5 photos)."
)
public class UpdateStatutRequest {

    @NotNull(message = "Le statut est requis")
    @Schema(description = "Nouveau statut cible.",
            example = "ACTIVE",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private StatutAnnonce statut;
}
