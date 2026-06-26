package com.djeffing.SpecZeta.security.oauth2;

import com.djeffing.SpecZeta.config.AppProperties;
import com.djeffing.SpecZeta.domain.user.dto.Token;
import com.djeffing.SpecZeta.security.CustomUserPrincipal;
import com.djeffing.SpecZeta.security.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String REDIRECT_URI_PARAM = "redirect_uri";

    private final JwtTokenProvider tokenProvider;
    private final AppProperties appProperties;

    /**
     * Hook Spring Security invoqué dès qu'un login OAuth2 réussit.
     * Calcule l'URL de redirection finale (avec le JWT généré en query string)
     * puis envoie la redirection HTTP au navigateur.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, authentication);

        if (response.isCommitted()) {
            log.debug("La réponse est déjà committée, impossible de rediriger vers {}", targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Détermine l'URL finale vers laquelle rediriger le navigateur :
     * <ol>
     *   <li>privilégie un {@code redirect_uri} fourni par le frontend s'il est dans la liste blanche ;</li>
     *   <li>sinon prend la première URI autorisée déclarée dans {@code app.oauth2.authorized-redirect-uris} ;</li>
     *   <li>génère un JWT et l'attache en paramètre {@code ?token=…} sur cette URL.</li>
     * </ol>
     */
    protected String determineTargetUrl(HttpServletRequest request, Authentication authentication) {
        String redirectUri = request.getParameter(REDIRECT_URI_PARAM);
        String targetUrl = (redirectUri != null && isAuthorizedRedirectUri(redirectUri))
                ? redirectUri
                : appProperties.getOauth2().getAuthorizedRedirectUris().stream()
                        .findFirst()
                        .orElse("/");

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        Token token = tokenProvider.generateToken(principal.getId(), principal.getEmail(),false);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();
    }

    /**
     * Vérifie qu'une URI demandée par le client est autorisée en comparant
     * uniquement le couple {@code (host, port)}. On ignore le path pour permettre
     * différentes routes de callback, tout en bloquant les open redirects vers
     * un domaine externe.
     */
    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
        List<String> authorized = appProperties.getOauth2().getAuthorizedRedirectUris();
        return authorized.stream().anyMatch(authorizedUri -> {
            URI authUri = URI.create(authorizedUri);
            return authUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                    && authUri.getPort() == clientRedirectUri.getPort();
        });
    }
}
