package com.djeffing.SpecZeta.domain.favoris.mapper;

import com.djeffing.SpecZeta.domain.annonce.mapper.AnnonceMapper;
import com.djeffing.SpecZeta.domain.favoris.dto.AlerteResponse;
import com.djeffing.SpecZeta.domain.favoris.dto.FavoriResponse;
import com.djeffing.SpecZeta.domain.favoris.entity.AlerteRecherche;
import com.djeffing.SpecZeta.domain.favoris.entity.Favori;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = AnnonceMapper.class)
public interface FavorisMapper {

    /**
     * Convertit un {@link Favori} en sa DTO réponse en embarquant l'annonce
     * en projection allégée ({@code AnnonceListResponse}) via {@link AnnonceMapper}.
     */
    @Mapping(target = "annonce", source = "annonce")
    FavoriResponse toResponse(Favori favori);

    /**
     * Convertit une {@link AlerteRecherche} en DTO réponse. Mapping 1:1 sans
     * transformation particulière puisque les critères de recherche sont stockés
     * tels quels en base.
     */
    AlerteResponse toAlerteResponse(AlerteRecherche alerte);
}
