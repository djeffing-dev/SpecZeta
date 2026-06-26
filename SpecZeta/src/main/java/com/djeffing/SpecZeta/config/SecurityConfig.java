package com.djeffing.SpecZeta.config;

import com.djeffing.SpecZeta.security.CustomUserDetailsService;
import com.djeffing.SpecZeta.security.JwtAuthenticationFilter;
import com.djeffing.SpecZeta.security.oauth2.CustomOAuth2UserService;
import com.djeffing.SpecZeta.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.djeffing.SpecZeta.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
    private final AppProperties appProperties;

    /**
     * Encoder de mot de passe utilisé par {@code DaoAuthenticationProvider}
     * (hash des passwords à l'inscription, comparaison au login).
     * BCrypt est sélectionné pour son sel intégré et son coût ajustable.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provider d'authentification pour le login local (email + mot de passe).
     * Branche le {@code CustomUserDetailsService} et l'encoder BCrypt déclarés
     * plus haut, de manière à ce que Spring Security sache comment vérifier
     * un couple identifiants.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Expose le {@code AuthenticationManager} géré par Spring sous forme de bean
     * pour qu'il puisse être injecté dans le futur {@code AuthController}
     * (méthode {@code /api/auth/login}).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Chaîne de filtres centrale de Spring Security :
     * <ul>
     *   <li>désactive CSRF et form login (API stateless en JWT) ;</li>
     *   <li>active CORS sur les origines autorisées ;</li>
     *   <li>déclare les routes publiques (auth, oauth2, GET annonces, GET search, WebSocket) ;</li>
     *   <li>câble le flow OAuth2 (userService custom + success/failure handlers) ;</li>
     *   <li>insère le filtre JWT avant le filtre standard de Spring Security ;</li>
     *   <li>enregistre le {@code DaoAuthenticationProvider} pour le login local.</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/api/otp/**",
                                "/error",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/annonces/**", "/api/search/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(ui -> ui.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Construit la politique CORS appliquée à toutes les routes :
     * lit les origines autorisées depuis {@code app.cors.allowed-origins}, autorise
     * les principales méthodes HTTP et tous les en-têtes, active les credentials
     * (cookies / Authorization), et expose l'en-tête {@code Authorization} au frontend
     * pour pouvoir lire le JWT en réponse.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = appProperties.getCors().getAllowedOrigins();
        if (origins == null || origins.isEmpty()) {
            origins = List.of("http://localhost:4200", "http://localhost:3000");
        }
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
