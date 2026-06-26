package com.djeffing.SpecZeta.security;

import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Charge un utilisateur par son email pour le login local (formulaire / endpoint).
     * Appelé par {@code DaoAuthenticationProvider} lors de la vérification du mot de passe.
     * Lève {@link UsernameNotFoundException} si aucun compte ne correspond.
     *
     * @param email identifiant utilisé comme « username » dans l'application
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun utilisateur trouvé avec l'email : " + email));
        return CustomUserPrincipal.from(user);
    }

    /**
     * Charge un utilisateur par son id. Appelé par le {@code JwtAuthenticationFilter}
     * une fois le JWT validé, puisque le subject du token contient l'id et non l'email.
     * Permet d'éviter une requête supplémentaire sur l'email.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun utilisateur trouvé avec l'id : " + id));
        return CustomUserPrincipal.from(user);
    }
}
