package com.djeffing.SpecZeta.config;

import com.djeffing.SpecZeta.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider tokenProvider;
    private final AppProperties appProperties;

    /**
     * Configure les préfixes du broker STOMP :
     * <ul>
     *   <li>{@code /app} : préfixe pour les messages entrants côté serveur (futurs @MessageMapping) ;</li>
     *   <li>{@code /topic} : broadcast à plusieurs abonnés ;</li>
     *   <li>{@code /queue} : files point-à-point — utilisées via {@code /user/{id}/queue/messages}
     *       pour notifier un utilisateur précis depuis le {@code MessagingService}.</li>
     * </ul>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Déclare l'endpoint de handshake WebSocket sur {@code /ws}.
     * Les origines autorisées sont lues depuis {@code app.cors.allowed-origins}
     * pour rester cohérent avec la politique CORS HTTP.
     * Active aussi le fallback SockJS pour les navigateurs sans WebSocket natif.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        List<String> origins = appProperties.getCors().getAllowedOrigins();
        String[] originPatterns = (origins == null || origins.isEmpty())
                ? new String[]{"http://localhost:4200", "http://localhost:3000"}
                : origins.toArray(new String[0]);

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(originPatterns)
                .withSockJS();
    }

    /**
     * Branche un intercepteur sur le canal entrant pour authentifier les
     * connexions STOMP : à la trame {@code CONNECT}, lit l'en-tête
     * {@code Authorization: Bearer <JWT>} et associe le {@link Principal}
     * correspondant à la session. Les frames suivantes héritent ainsi de l'identité.
     */
    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new JwtChannelInterceptor(tokenProvider));
    }

    /**
     * Intercepteur de canal qui valide le JWT à la phase {@code CONNECT}
     * et place une {@link UsernamePasswordAuthenticationToken} sur l'accessor
     * pour que Spring sache à qui adresser les messages utilisateur.
     */
    @RequiredArgsConstructor
    static class JwtChannelInterceptor implements ChannelInterceptor {

        private final JwtTokenProvider tokenProvider;

        /**
         * Inspecte chaque message entrant. Si c'est un CONNECT, extrait et valide
         * le token, puis fixe l'utilisateur pour la session. Les autres trames
         * passent simplement sans modification.
         */
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                return message;
            }

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("Connexion STOMP sans token Authorization");
                return message;
            }
            String token = authHeader.substring("Bearer ".length());

            if (!tokenProvider.validateToken(token)) {
                log.debug("Token JWT invalide sur CONNECT STOMP");
                return message;
            }

            Long userId = tokenProvider.getUserIdFromToken(token);
            Principal principal = new UsernamePasswordAuthenticationToken(
                    String.valueOf(userId), null, Collections.emptyList());
            accessor.setUser(principal);
            return message;
        }
    }
}
