package com.djeffing.SpecZeta.domain.user.service;

import com.djeffing.SpecZeta.domain.user.dto.AuthResponse;
import com.djeffing.SpecZeta.domain.user.dto.LoginRequest;
import com.djeffing.SpecZeta.domain.user.dto.Token;
import com.djeffing.SpecZeta.domain.user.dto.UserRegistrationRequest;
import com.djeffing.SpecZeta.domain.user.entity.RefreshToken;
import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.enums.AuthProvider;
import com.djeffing.SpecZeta.domain.user.mapper.UserMapper;
import com.djeffing.SpecZeta.domain.user.repository.RefreshTokenRepository;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import com.djeffing.SpecZeta.security.CustomUserPrincipal;
import com.djeffing.SpecZeta.security.JwtTokenProvider;
import com.djeffing.SpecZeta.shared.exception.BadRequestException;
import com.djeffing.SpecZeta.shared.exception.ResourceNotFoundException;
import com.djeffing.SpecZeta.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OTPVerificationService otpService;

    /**
     * Inscrit un nouvel utilisateur via la voie locale (email + mot de passe).
     * Vérifie l'unicité de l'email et du pseudo, hash le mot de passe en BCrypt,
     * persiste l'entité puis génère un Code de vérification pour authentifier le mail de l'utilisateur session.
     *
     * @throws BadRequestException si l'email ou le pseudo est déjà pris
     */
    @Transactional
    public ResponseEntity<?> register(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Cet email est déjà utilisé");
        }
        if (userRepository.existsByPseudo(request.getPseudo())) {
            throw new BadRequestException("Ce pseudo est déjà pris");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .pseudo(request.getPseudo())
                .ville(request.getVille())
                .provider(AuthProvider.LOCAL)
                .emailVerified(false)
                .ratingMoyenne(0.0)
                .nombreEvaluations(0)
                .build();

        User saved = userRepository.save(user);

        // Création et envoi du code de vérification d'email
        String plainCode = otpService.createOtp(saved);
        otpService.sendOtpEmail(saved, plainCode);

        log.info("Nouvel utilisateur inscrit : id={}, email={}, code de vérification envoyé.",
                saved.getId(), saved.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("details", "Compte créé. Un code de vérification a été envoyé à votre adresse email.");

        return ResponseEntity.ok(response);
    }

    /**
     * Authentifie un utilisateur via email + mot de passe. Délègue la vérification
     * au {@link AuthenticationManager} (qui passe par le {@code DaoAuthenticationProvider}
     * et BCrypt), puis génère un JWT à renvoyer au client.
     *
     * @throws org.springframework.security.authentication.BadCredentialsException si les identifiants sont invalides
     */
    public ResponseEntity<?> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        Token token = tokenProvider.generateToken(principal.getId(), principal.getEmail(), request.getRememberMe());

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));

        if(!user.getEmailVerified()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Veuillez vérifier votre adresse email avant de vous connecter.");
        }

        log.info("Connexion réussie : id={}, email={}", user.getId(), user.getEmail());
        return ResponseEntity.ok(buildResponse(user, token));
    }


    public Token getNewAssesstokenByRefreshToken(String refreshToken){
        // Validation de la signature et de l'expiration du JWT via le JwtService
        if(tokenProvider.validateToken(refreshToken)){
            throw new UnauthorizedException("Le Refresh Token est invalide ou a expiré.");
        }
        // Extraction de l'identité de l'utilisateur
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        Boolean rememberMe = tokenProvider.getRememberMeFromToken(refreshToken);

        // Vérification en Base de Données (Sécurité anti-vol/révocation)
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh Token inconnu ou révoqué."));

        // Vérification de la cohérence et de l'expiration stockée en BDD
        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken); // Nettoyage de la BDD
            throw new UnauthorizedException("Le Refresh Token a expiré. Veuillez vous re-authentifier.");
        }

        // Récupération de l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));

        // 6. Génération du nouvel Access Token et refresh token
        return tokenProvider.generateToken(user.getId(), user.getEmail(), rememberMe);
    }

    /**
     * Construit la réponse d'authentification enrichie d'un résumé du profil
     * et de la durée de validité du token (en millisecondes).
     */
    private AuthResponse buildResponse(User user, Token token) {
        return AuthResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMs())
                .user(userMapper.toSummary(user))
                .build();
    }
}
