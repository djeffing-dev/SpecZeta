package com.djeffing.SpecZeta.security.oauth2;

import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.enums.AuthProvider;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import com.djeffing.SpecZeta.security.CustomUserPrincipal;
import com.djeffing.SpecZeta.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * Point d'entrée appelé par Spring Security après que l'utilisateur a validé
     * la fenêtre de consentement Google/Facebook. Délègue le chargement brut des
     * attributs à {@link DefaultOAuth2UserService}, puis enchaîne sur la logique
     * de création/mise à jour en base.
     *
     * @param userRequest contient le token d'accès et la configuration du provider
     * @return principal applicatif (implémente à la fois {@code UserDetails} et {@code OAuth2User})
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (OAuth2AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Erreur lors du traitement de l'utilisateur OAuth2", ex);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("oauth2_processing_error"), ex.getMessage(), ex);
        }
    }

    /**
     * Cœur de la logique de fédération d'identité : vérifie la présence de l'email,
     * puis applique l'un des trois chemins suivants :
     * <ul>
     *   <li>compte déjà lié au provider → met à jour la photo / le flag email_verified ;</li>
     *   <li>compte LOCAL existant avec le même email → lie le provider à ce compte ;</li>
     *   <li>aucun compte → crée un nouvel utilisateur avec un pseudo unique.</li>
     * </ul>
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.from(registrationId, oAuth2User.getAttributes());

        if (!StringUtils.hasText(userInfo.getEmail())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_missing"),
                    "L'email n'a pas été fourni par le provider OAuth2");
        }

        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase(Locale.ROOT));

        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getId())
                .map(existing -> updateUser(existing, userInfo))
                .orElseGet(() -> userRepository.findByEmail(userInfo.getEmail())
                        .map(existing -> linkProvider(existing, provider, userInfo))
                        .orElseGet(() -> createUser(provider, userInfo)));

        return CustomUserPrincipal.from(user, oAuth2User.getAttributes());
    }

    /**
     * Crée un nouveau compte utilisateur à partir des informations OAuth2.
     * Le pseudo est généré à partir du nom ou de la partie locale de l'email,
     * et un suffixe est ajouté en cas de collision (voir {@link #generateUniquePseudo}).
     * L'email est marqué vérifié puisqu'il a été validé par le provider.
     */
    private User createUser(AuthProvider provider, OAuth2UserInfo userInfo) {
        String pseudo = generateUniquePseudo(userInfo.getName(), userInfo.getEmail());

        User user = User.builder()
                .email(userInfo.getEmail())
                .pseudo(pseudo)
                .photoUrl(userInfo.getImageUrl())
                .provider(provider)
                .providerId(userInfo.getId())
                .emailVerified(true)
                .ratingMoyenne(0.0)
                .nombreEvaluations(0)
                .build();

        log.info("Création d'un nouvel utilisateur OAuth2 : email={}, provider={}", user.getEmail(), provider);
        return userRepository.save(user);
    }

    /**
     * Met à jour un utilisateur déjà lié au provider : rafraîchit l'avatar
     * si une nouvelle URL est renvoyée et confirme la vérification de l'email.
     */
    private User updateUser(User existing, OAuth2UserInfo userInfo) {
        if (StringUtils.hasText(userInfo.getImageUrl())) {
            existing.setPhotoUrl(userInfo.getImageUrl());
        }
        existing.setEmailVerified(true);
        return userRepository.save(existing);
    }

    /**
     * Lie un provider OAuth2 à un compte existant trouvé par email.
     * Refuse si l'utilisateur est déjà rattaché à un autre provider tiers
     * pour éviter qu'un attaquant ne détourne un compte via un second provider.
     */
    private User linkProvider(User existing, AuthProvider provider, OAuth2UserInfo userInfo) {
        if (existing.getProvider() != AuthProvider.LOCAL && existing.getProvider() != provider) {
            throw new BadRequestException(
                    "Cet email est déjà associé au provider " + existing.getProvider());
        }
        existing.setProvider(provider);
        existing.setProviderId(userInfo.getId());
        existing.setEmailVerified(true);
        if (StringUtils.hasText(userInfo.getImageUrl())) {
            existing.setPhotoUrl(userInfo.getImageUrl());
        }
        return userRepository.save(existing);
    }

    /**
     * Construit un pseudo unique :
     * <ol>
     *   <li>part du nom (ou de la partie locale de l'email à défaut) ;</li>
     *   <li>nettoie les caractères spéciaux et borne à 60 caractères ;</li>
     *   <li>ajoute un suffixe numérique en cas de collision ({@code …1}, {@code …2}…) ;</li>
     *   <li>au-delà de 5 tentatives, fallback sur un suffixe UUID pour garantir l'unicité.</li>
     * </ol>
     */
    private String generateUniquePseudo(String name, String email) {
        String base = StringUtils.hasText(name) ? name : email.split("@")[0];
        String candidate = base.replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase(Locale.ROOT);
        if (candidate.length() > 60) {
            candidate = candidate.substring(0, 60);
        }
        String pseudo = candidate;
        int attempt = 0;
        while (userRepository.existsByPseudo(pseudo)) {
            attempt++;
            if (attempt > 5) {
                pseudo = candidate + "-" + UUID.randomUUID().toString().substring(0, 6);
                break;
            }
            pseudo = candidate + attempt;
        }
        return pseudo;
    }
}
