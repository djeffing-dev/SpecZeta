package com.djeffing.SpecZeta.security;

import com.djeffing.SpecZeta.config.AppProperties;
import com.djeffing.SpecZeta.domain.user.dto.Token;
import com.djeffing.SpecZeta.domain.user.entity.RefreshToken;
import com.djeffing.SpecZeta.domain.user.repository.RefreshTokenRepository;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import com.djeffing.SpecZeta.shared.exception.ResourceNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long expirationMs;
    private final String issuer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    /**
     * Initialise la clé HMAC dérivée du secret configuré et lit les paramètres
     * (durée de validité, issuer) une fois pour toutes au démarrage de l'application.
     */
    public JwtTokenProvider(AppProperties props, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.signingKey = Keys.hmacShaKeyFor(deriveKeyBytes(props.getJwt().getSecret()));
        this.expirationMs = props.getJwt().getExpirationMs();
        this.issuer = props.getJwt().getIssuer();
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    /**
     * Génère un JWT signé HS512 contenant l'id utilisateur en {@code subject}
     * et l'email en claim privé. Durée de validité fixée par {@code app.jwt.expiration-ms}.
     *
     * @param userId identifiant de l'utilisateur authentifié
     * @param email  email associé (claim « email »)
     * @return chaîne compactée prête à être renvoyée au client
     */
    public String generateAccessToken(Long userId, String email, Boolean rememberMe) {
        Instant now = Instant.now();

        Instant expiration = rememberMe
                ? now.plus(Duration.ofHours(24))
                : now.plus(Duration.ofMinutes(30));
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .claim("email", email)
                .claim("remember_me", rememberMe)
                .issuedAt(Date.from(now))
                .expiration(Date.from(Date.from(expiration).toInstant()))
                .signWith(signingKey)
                .compact();
    }

    public Token generateToken(Long userId, String email, Boolean rememberMe) {
        String accessToken = this.generateAccessToken(userId,email,rememberMe);
        String refreshToken = this.generateRefreshToken(userId,email,rememberMe);

        return  Token.builder()
                .accessToken(accessToken)
                .RefreshToken(refreshToken)
                .build();
    }

    public String generateRefreshToken(Long userId, String email, Boolean rememberMe){
        Instant now = Instant.now();

        Instant expiration = rememberMe
                ? now.plus(Duration.ofDays(7))
                : now.plus(Duration.ofHours(1));

        // Create refresh token
        String refreshToken =  Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .claim("email", email)
                .claim("remember_me", rememberMe)
                .issuedAt(Date.from(now))
                .expiration(Date.from(Date.from(expiration).toInstant()))
                .signWith(signingKey)
                .compact();



        // Verifier si un refresh token existe pour cette utilisateur
        Optional<RefreshToken> lastRefreshToken = refreshTokenRepository
                .findByUserId(userId);
        if(lastRefreshToken.isPresent()){
            RefreshToken newRefreshToken = lastRefreshToken.get();
            newRefreshToken.setToken(refreshToken);
            newRefreshToken.setExpiryDate(expiration);
            refreshTokenRepository.save(newRefreshToken);
            return refreshToken;
        }

        // Save Refresh token
        RefreshToken refreshTokenSaved = RefreshToken.builder()
                .token(refreshToken)
                .expiryDate(expiration)
                .user(userRepository.findByEmail(email).orElseThrow())
                .build();
        refreshTokenRepository.save(refreshTokenSaved);

        return  refreshToken;

    }


    /**
     * Extrait l'id utilisateur (subject) d'un token déjà validé.
     * Appelé par le {@code JwtAuthenticationFilter} pour recharger l'utilisateur.
     */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }
    public Boolean getRememberMeFromToken(String token){
        return parseClaims(token).get("remember_me", Boolean.class);
    }

    /**
     * Extrait l'email présent dans le claim privé « email » du token.
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).get("email", String.class);
    }

    /**
     * Vérifie la signature et la non-expiration du JWT.
     * Toute exception jjwt (signature, expiration, format) entraîne {@code false}
     * sans propager d'erreur à l'appelant.
     *
     * @param token token brut reçu dans l'en-tête {@code Authorization}
     * @return {@code true} si le token est valide et utilisable
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT invalide : {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Expose la durée de validité configurée (utile pour renvoyer
     * {@code expiresIn} dans la réponse d'authentification).
     */
    public long getExpirationMs() {
        return expirationMs;
    }

    /**
     * Parse et vérifie le token en une seule étape : signature, expiration,
     * issuer. Retourne les claims décodés.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Dérive 64 octets (512 bits) depuis le secret via SHA-512.
     * Garantit que la clé satisfait toujours les exigences de longueur de HS512,
     * même si l'utilisateur a configuré un secret court.
     */
    private static byte[] deriveKeyBytes(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret est requis");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 indisponible", e);
        }
    }
}
