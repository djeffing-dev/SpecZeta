package com.djeffing.SpecZeta.security;

import com.djeffing.SpecZeta.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CustomUserPrincipal implements UserDetails, OAuth2User {

    private final Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    /**
     * Constructeur principal utilisé par les méthodes {@code from(...)} statiques.
     * On stocke explicitement l'id pour pouvoir l'utiliser dans les contrôleurs
     * sans recharger l'entité depuis la base.
     */
    public CustomUserPrincipal(Long id,
                               String email,
                               String password,
                               Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Construit un principal à partir d'une entité {@code User} pour le flow local
     * (login email/password). Attribue un rôle unique {@code ROLE_USER}.
     */
    public static CustomUserPrincipal from(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }

    /**
     * Variante OAuth2 : enrichit le principal des attributs renvoyés par le provider
     * (sub, email, picture…) pour qu'ils restent accessibles côté SecurityContext.
     */
    public static CustomUserPrincipal from(User user, Map<String, Object> attributes) {
        CustomUserPrincipal principal = from(user);
        principal.attributes = attributes != null ? attributes : new HashMap<>();
        return principal;
    }

    /**
     * Implémentation OAuth2User : Spring attend un identifiant stable.
     * On expose l'id base de données pour rester cohérent avec le subject du JWT.
     */
    @Override
    public String getName() {
        return String.valueOf(id);
    }

    /**
     * Attributs OAuth2 bruts (vide pour un login local).
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Rôles/permissions de l'utilisateur exploités par {@code @PreAuthorize}.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Hash BCrypt du mot de passe (null pour un compte OAuth2 sans mot de passe local).
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Identifiant utilisé par Spring Security pour authentifier
     * (l'email tient lieu de username dans cette application).
     */
    @Override
    public String getUsername() {
        return email;
    }
}
