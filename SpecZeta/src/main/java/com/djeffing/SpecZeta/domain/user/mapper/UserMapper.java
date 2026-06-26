package com.djeffing.SpecZeta.domain.user.mapper;

import com.djeffing.SpecZeta.domain.user.dto.UserProfileResponse;
import com.djeffing.SpecZeta.domain.user.dto.UserSummaryResponse;
import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Convertit une entité {@link User} en {@link UserSummaryResponse}.
     * Utilisé pour embarquer la fiche vendeur dans les réponses d'annonces ou de ratings.
     */
    UserSummaryResponse toSummary(User user);

    /**
     * Convertit une entité {@link User} et son profil étendu optionnel en
     * {@link UserProfileResponse}. Aplati le {@link UserProfile} dans la réponse :
     * les champs biographie/adresse/etc. y sont copiés au même niveau que les
     * champs de l'utilisateur de base.
     */
    @Mapping(target = "biographie", source = "profile.biographie")
    @Mapping(target = "dateNaissance", source = "profile.dateNaissance")
    @Mapping(target = "adresse", source = "profile.adresse")
    @Mapping(target = "codePostal", source = "profile.codePostal")
    @Mapping(target = "pays", source = "profile.pays")
    @Mapping(target = "siteWeb", source = "profile.siteWeb")
    @Mapping(target = "id", source = "user.id") // 👈 Résout l'ambiguïté de l'id
    @Mapping(target = "createdAt", source = "user.createdAt") // 👈 Résout l'ambiguïté du createdAt
    UserProfileResponse toProfileResponse(User user, UserProfile profile);
}
