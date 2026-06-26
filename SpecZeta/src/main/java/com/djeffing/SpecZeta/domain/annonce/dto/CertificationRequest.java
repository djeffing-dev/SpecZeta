package com.djeffing.SpecZeta.domain.annonce.dto;

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
        name = "CertificationRequest",
        description = "Payload de soumission d'une certification benchmark pour une annonce. "
                + "L'URL fournie est validée contre une liste blanche de domaines reconnus "
                + "(Geekbench, 3DMark, CPU-Z, etc.) afin d'éviter les faux résultats."
)
public class CertificationRequest {

    @NotBlank(message = "Le type de benchmark est requis")
    @Size(max = 30)
    @Schema(description = "Identifiant du benchmark utilisé.",
            example = "GEEKBENCH_6",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 30,
            allowableValues = {"GEEKBENCH_5", "GEEKBENCH_6", "3DMARK_TIMESPY", "3DMARK_FIRESTRIKE", "CPU_Z", "CINEBENCH_R23", "AUTRE"})
    private String typeBenchmark;

    @Size(max = 1000)
    @Schema(description = "URL publique du résultat du benchmark.",
            example = "https://browser.geekbench.com/v6/cpu/12345678",
            maxLength = 1000,
            format = "uri")
    private String urlBenchmark;

    @Schema(description = "Score monocœur (le cas échéant).", example = "2800")
    private Integer scoreMonocoeur;

    @Schema(description = "Score multicœur (le cas échéant).", example = "14500")
    private Integer scoreMulticoeur;

    @Schema(description = "Score GPU (le cas échéant).", example = "11000")
    private Integer scoreGpu;

    @Size(max = 1000)
    @Schema(description = "URL d'un fichier de log brut associé au benchmark (optionnel).",
            example = "https://dl.dropboxusercontent.com/s/abc/benchmark.log",
            maxLength = 1000,
            format = "uri")
    private String logFileUrl;

    @Schema(description = "Contenu JSON brut du log (optionnel — alternative à `logFileUrl`).",
            example = "{\"runId\":\"abc\",\"score\":14500}")
    private String logDataJson;
}
