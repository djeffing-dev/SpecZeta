package com.djeffing.SpecZeta.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Dropbox dropbox = new Dropbox();
    private final Oauth2 oauth2 = new Oauth2();
    private final Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs = 86_400_000L;
        private String issuer = "speczeta";
    }

    @Data
    public static class Dropbox {
        private String accessToken;
        private String basePath = "/annonces";
        private String apiUrl = "https://api.dropboxapi.com";
        private String contentUrl = "https://content.dropboxapi.com";
    }

    @Data
    public static class Oauth2 {
        private List<String> authorizedRedirectUris = new ArrayList<>();
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }
}
