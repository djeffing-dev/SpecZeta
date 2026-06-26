package com.djeffing.SpecZeta.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "PagedResponse",
        description = "Représentation paginée d'une collection d'éléments. Format léger destiné à "
                + "remplacer l'objet `Page` de Spring Data dans les réponses publiques de l'API."
)
public class PagedResponse<T> {

    @Schema(description = "Éléments contenus dans la page courante.")
    private List<T> content;

    @Schema(description = "Index de la page courante (base 0).", example = "0")
    private int page;

    @Schema(description = "Nombre d'éléments par page.", example = "20")
    private int size;

    @Schema(description = "Nombre total d'éléments à travers toutes les pages.", example = "137")
    private long totalElements;

    @Schema(description = "Nombre total de pages disponibles.", example = "7")
    private int totalPages;

    @Schema(description = "`true` si la page courante est la première.", example = "true")
    private boolean first;

    @Schema(description = "`true` si la page courante est la dernière.", example = "false")
    private boolean last;

    public static <E, T> PagedResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return PagedResponse.<T>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public static <T> PagedResponse<T> of(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
