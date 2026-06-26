package com.djeffing.SpecZeta.domain.user.service;

import com.djeffing.SpecZeta.domain.user.dto.AuthResponse;
import com.djeffing.SpecZeta.domain.user.dto.Token;
import com.djeffing.SpecZeta.domain.user.dto.UserSummaryResponse;
import com.djeffing.SpecZeta.domain.user.entity.User;
import com.djeffing.SpecZeta.domain.user.repository.UserRepository;
import com.djeffing.SpecZeta.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Transactional
@Service
@RequiredArgsConstructor
public class GoogleOauth2Service {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private  String redirectionUrl;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final NetHttpTransport transport = new NetHttpTransport();
    private final GsonFactory jsonFactory = new GsonFactory();

    //Generation de l'url d'authentification via google
    public String createUrl(){
        return new GoogleAuthorizationCodeRequestUrl(clientId,
                redirectionUrl,
                Arrays.asList("email","profile","openid"))
                .build();
    }

    // Recuperer le token d'authentification
    public ResponseEntity<?> callBack(String code){
        try {
            // ✅ Utiliser les instances de classe
            var tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    transport,
                    jsonFactory,
                    clientId,
                    clientSecret,
                    code,
                    redirectionUrl
            ).execute();
            String idTokenString = tokenResponse.getIdToken();
            GoogleIdToken idToken = GoogleIdToken.parse(jsonFactory, idTokenString);

            // Vérification de la validité du token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                    .Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            // ✅ Gérer le cas où la vérification échoue
            if(!verifier.verify(idToken)){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid ID token"));
            }

            GoogleIdToken.Payload payload =  idToken.getPayload();

            // Extraire les informations utilisateurs
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String profilUrl = (String) payload.get("picture");

            // Trouver ou créer l'utilisateur
            User user = findOrCreateUser(email,name,profilUrl);

            // Autehtificaiton de l'utilsateur.
            AuthResponse authResponse = authUser(user);
            return ResponseEntity.ok(authResponse);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Failed to authenticate with Google: " + e.getMessage()));
        } catch (GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token verification failed: " + e.getMessage()));
        }
    }

    private User findOrCreateUser(String email, String pseudo, String profilUrl){
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setPseudo(pseudo);
                    user.setPhotoUrl(profilUrl);
                    user.setEmailVerified(true);
                    return userRepository.save(user);
                });
    }

    private AuthResponse authUser(User user){
        Token token =
                jwtTokenProvider.generateToken(user.getId(),user.getEmail(),true);

        return AuthResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .user(UserSummaryResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .pseudo(user.getPseudo())
                        .photoUrl(user.getPhotoUrl())
                        .ville(user.getVille())
                        .build())
                .build();
    }
}
