package com.djeffing.SpecZeta.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "ApiResponse",
        description = "Enveloppe standard utilisée par tous les endpoints de l'API SpecZeta. "
                + "Contient un indicateur de succès, un message lisible, le payload métier (`data`), "
                + "une éventuelle liste d'erreurs détaillées, ainsi qu'un horodatage de génération."
)
public class ApiResponse<T> {

    @Schema(description = "`true` si la requête a abouti, `false` sinon.", example = "true")
    private boolean success;

    @Schema(description = "Message lisible accompagnant la réponse (succès ou erreur).",
            example = "Annonce créée")
    private String message;

    @Schema(description = "Charge utile métier de la réponse. Peut être `null` pour les opérations sans payload.")
    private T data;

    @Schema(description = "Liste des erreurs détaillées (typiquement les erreurs de validation champ par champ). "
            + "Présent uniquement en cas d'échec.",
            example = "[\"email: format invalide\", \"password: minimum 8 caractères\"]")
    private List<String> errors;

    @Schema(description = "Horodatage de génération de la réponse (ISO-8601).",
            example = "2026-06-03T10:15:30")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> message(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static ApiResponse<Void> error(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static ApiResponse<Void> error(String message, List<String> errors) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
