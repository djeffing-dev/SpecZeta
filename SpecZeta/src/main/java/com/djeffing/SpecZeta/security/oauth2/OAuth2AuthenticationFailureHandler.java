package com.djeffing.SpecZeta.security.oauth2;

import com.djeffing.SpecZeta.config.AppProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final AppProperties appProperties;

    /**
     * Hook Spring Security invoqué quand le flow OAuth2 échoue
     * (consentement refusé, email manquant, provider non supporté…).
     * Log l'erreur côté serveur et redirige le navigateur vers la première URL
     * autorisée avec un paramètre {@code ?error=…} URL-encodé pour que le frontend
     * puisse afficher un message lisible.
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("Échec de l'authentification OAuth2", exception);

        String redirectUri = appProperties.getOauth2().getAuthorizedRedirectUris().stream()
                .findFirst()
                .orElse("/");

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", java.net.URLEncoder.encode(
                        exception.getLocalizedMessage(), StandardCharsets.UTF_8))
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
