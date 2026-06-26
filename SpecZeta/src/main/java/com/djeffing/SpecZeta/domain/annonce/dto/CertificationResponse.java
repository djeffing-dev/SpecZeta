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
        name = "CertificationResponse",
        description = "Vue lecture d'une certification benchmark associée à une annonce."
)
public class CertificationResponse {

    @Schema(description = "Identifiant interne.", example = "55")
    private Long id;

    @Schema(description = "Type de benchmark.", example = "GEEKBENCH_6")
    private String typeBenchmark;

    @Schema(description = "URL du résultat publié.",
            example = "https://browser.geekbench.com/v6/cpu/12345678",
            format = "uri")
    private String urlBenchmark;

    @Schema(description = "Score monocœur.", example = "2800")
    private Integer scoreMonocoeur;

    @Schema(description = "Score multicœur.", example = "14500")
    private Integer scoreMulticoeur;

    @Schema(description = "Score GPU.", example = "11000")
    private Integer scoreGpu;

    @Schema(description = "URL du fichier de log brut associé.",
            example = "https://dl.dropboxusercontent.com/s/abc/benchmark.log",
            format = "uri")
    private String logFileUrl;

    @Schema(description = "Horodatage de vérification de la certification.",
            example = "2026-06-03T10:18:42")
    private LocalDateTime verifiedAt;
}
