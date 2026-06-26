package com.djeffing.SpecZeta.domain.annonce.dto;

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
        name = "AnnonceMediaResponse",
        description = "Métadonnées d'une photo d'annonce stockée sur Dropbox."
)
public class AnnonceMediaResponse {

    @Schema(description = "Identifiant interne de la photo.", example = "8021")
    private Long id;

    @Schema(description = "URL publique Dropbox de la photo.",
            example = "https://dl.dropboxusercontent.com/s/abc/annonce-1024-1.jpg",
            format = "uri")
    private String dropboxUrl;

    @Schema(description = "Position d'affichage dans la galerie (0 = photo principale).",
            example = "0",
            minimum = "0", maximum = "4")
    private Integer ordre;

    @Schema(description = "Horodatage d'upload de la photo.", example = "2026-05-21T14:08:35")
    private LocalDateTime uploadedAt;
}
