package com.djeffing.SpecZeta.domain.user.repository;

import com.djeffing.SpecZeta.domain.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Recherche le profil étendu d'un utilisateur via l'id du compte propriétaire.
     * Optional vide si le profil n'a pas encore été initialisé (création paresseuse
     * à la première mise à jour).
     */
    Optional<UserProfile> findByUserId(Long userId);
}
