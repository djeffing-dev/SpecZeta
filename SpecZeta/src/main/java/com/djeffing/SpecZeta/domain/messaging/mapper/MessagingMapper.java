package com.djeffing.SpecZeta.domain.messaging.mapper;

import com.djeffing.SpecZeta.domain.messaging.dto.MessageResponse;
import com.djeffing.SpecZeta.domain.messaging.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessagingMapper {

    /**
     * Convertit une entité {@link Message} en DTO réponse. Aplatit l'expéditeur
     * en deux champs simples ({@code expediteurId} + {@code expediteurPseudo})
     * pour éviter de charger un objet utilisateur complet dans la réponse.
     */
    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "expediteurId", source = "expediteur.id")
    @Mapping(target = "expediteurPseudo", source = "expediteur.pseudo")
    MessageResponse toResponse(Message message);
}
