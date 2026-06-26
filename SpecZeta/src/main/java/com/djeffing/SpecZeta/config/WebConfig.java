package com.djeffing.SpecZeta.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebMvc
public class WebConfig {
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterFilterRegistrationBean() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration configuration = new CorsConfiguration(); // Création de la configuration CORS à appliquer
        configuration.setAllowCredentials(true); // Permet l'envoi de cookies, tokens ou authentification dans les requêtes CORS

        // Autorise uniquement les requêtes venant de l'application Angular locale
        configuration.addAllowedOrigin("http://localhost:4200");
        configuration.addAllowedOrigin("http://localhost:8080");
        configuration.addAllowedOrigin("http://192.168.4.24:4200");
        configuration.addAllowedOrigin("http://192.168.4.33:4200");
        configuration.addAllowedOrigin("https://angular-ai-web-taky-git-main-jeffingdevs-projects.vercel.app/");
        configuration.addAllowedOrigin("https://angular-ai-web-taky.vercel.app/");
        configuration.addAllowedOrigin("https://www.virbray-ai.com/"); // adresse officielle
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setExposedHeaders(List.of("x-ratelimit-limit", "x-ratelimit-remaining", "x-ratelimit-reset"));


        // Spécifie les en-têtes HTTP autorisés dans les requêtes CORS
        configuration.setAllowedHeaders(Arrays.asList(
                HttpHeaders.AUTHORIZATION, // Pour les tokens JWT
                HttpHeaders.CONTENT_TYPE, // Pour indiquer le type de contenu (JSON, etc.)
                HttpHeaders.ACCEPT  // Pour indiquer les types de réponses acceptées
        ));

        // Spécifie les méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),         // Lire des ressources
                HttpMethod.POST.name(),        // Créer des ressources
                HttpMethod.PUT.name(),         // Mettre à jour des ressources
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name()       // Supprimer des ressources
        ));

        // Durée en secondes pendant la quel la réponse du preflight peut être mise en cache
        configuration.setMaxAge(3600L); // 1 heure

        // Applique la configuration CORS à toutes les URL de l'application
        source.registerCorsConfiguration("/**", configuration);

        // Création et enregistrement du filtre CORS avec la configuration définie
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));

        // Définition de la priorité d'exécution du filtre (négatif = priorité élevée)
        bean.setOrder(-102);

        return bean;
    }
}

