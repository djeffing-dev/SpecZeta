package com.djeffing.SpecZeta.domain.user.repository;

import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son email (unique en base).
     * Utilisé par le {@code CustomUserDetailsService} lors du login local.
     */
    Optional<User> findByEmail(String email);

    /**
     * Récupère l'utilisateur déjà lié à un compte OAuth2 (Google/Facebook).
     * La paire {@code (provider, providerId)} est unique à l'échelle du provider.
     */
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    /**
     * Vérifie l'existence d'un email avant inscription pour éviter une exception
     * de contrainte unique côté base de données.
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie l'existence d'un pseudo. Utilisé lors de la génération automatique
     * de pseudo depuis OAuth2 pour gérer les collisions.
     */
    boolean existsByPseudo(String pseudo);
}
