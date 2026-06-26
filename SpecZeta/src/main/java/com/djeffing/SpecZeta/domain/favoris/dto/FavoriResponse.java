package com.djeffing.SpecZeta.domain.favoris.dto;

import com.djeffing.SpecZeta.domain.annonce.dto.AnnonceListResponse;
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
        name = "FavoriResponse",
        description = "Vue lecture d'une entrée favorite de l'utilisateur courant, incluant un "
                + "résumé de l'annonce associée."
)
public class FavoriResponse {

    @Schema(description = "Identifiant unique de l'entrée favori.", example = "501")
    private Long id;

    @Schema(description = "Date d'ajout aux favoris.", example = "2026-06-02T18:42:11")
    private LocalDateTime createdAt;

    @Schema(description = "Résumé de l'annonce mise en favori.")
    private AnnonceListResponse annonce;
}
