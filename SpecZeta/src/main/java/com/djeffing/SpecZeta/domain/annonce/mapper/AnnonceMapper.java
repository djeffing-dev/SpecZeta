package com.djeffing.SpecZeta.domain.annonce.mapper;

import com.djeffing.SpecZeta.domain.annonce.dto.AnnonceListResponse;
import com.djeffing.SpecZeta.domain.annonce.dto.AnnonceMediaResponse;
import com.djeffing.SpecZeta.domain.annonce.dto.AnnonceResponse;
import com.djeffing.SpecZeta.domain.annonce.dto.CertificationResponse;
import com.djeffing.SpecZeta.domain.annonce.dto.FicheTechniqueRequest;
import com.djeffing.SpecZeta.domain.annonce.dto.FicheTechniqueResponse;
import com.djeffing.SpecZeta.domain.annonce.entity.Annonce;
import com.djeffing.SpecZeta.domain.annonce.entity.AnnonceMedia;
import com.djeffing.SpecZeta.domain.annonce.entity.CertificationBenchmark;
import com.djeffing.SpecZeta.domain.annonce.entity.FicheTechnique;
import com.djeffing.SpecZeta.domain.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface AnnonceMapper {

    /**
     * Convertit une entité {@link Annonce} en réponse complète pour la page détail.
     * Inclut le vendeur (via {@link UserMapper}), la fiche technique, la certification
     * et la liste des photos triées par {@code ordre}.
     */
    @Mapping(target = "vendeur", source = "vendeur")
    AnnonceResponse toResponse(Annonce annonce);

    /**
     * Convertit une {@link Annonce} en projection allégée pour les listes/résultats
     * de recherche. Ne retient que la première photo et l'identité minimale du vendeur.
     */
    @Mapping(target = "photoPrincipaleUrl",
            expression = "java(annonce.getMedias() == null || annonce.getMedias().isEmpty() ? null : annonce.getMedias().get(0).getDropboxUrl())")
    @Mapping(target = "vendeurPseudo", source = "vendeur.pseudo")
    @Mapping(target = "vendeurVille", source = "vendeur.ville")
    AnnonceListResponse toListResponse(Annonce annonce);

    /**
     * Convertit une {@link FicheTechnique} en sa DTO de réponse
     * (sans les données brutes {@code rawDataJson}, qui ne sont pas exposées côté API).
     */
    FicheTechniqueResponse toFicheResponse(FicheTechnique fiche);

    /**
     * Convertit une {@link CertificationBenchmark} en sa DTO de réponse
     * (sans le JSON brut des logs, conservé côté serveur uniquement).
     */
    CertificationResponse toCertificationResponse(CertificationBenchmark cert);

    /**
     * Convertit un {@link AnnonceMedia} en DTO réponse.
     */
    AnnonceMediaResponse toMediaResponse(AnnonceMedia media);

    /**
     * Construit une {@link FicheTechnique} à partir de la requête utilisateur.
     * L'annonce ({@code annonce}) sera rattachée par le service via
     * {@code Annonce.attachFicheTechnique(...)} avant la persistance.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "annonce", ignore = true)
    FicheTechnique toEntity(FicheTechniqueRequest request);

    /**
     * Convertit en masse une liste de médias en liste de DTOs.
     */
    List<AnnonceMediaResponse> toMediaResponses(List<AnnonceMedia> medias);
}
