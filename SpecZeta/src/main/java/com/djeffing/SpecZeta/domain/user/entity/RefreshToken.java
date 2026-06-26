package com.djeffing.SpecZeta.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "Refresh_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // L'utilisateur associé (Relation Many-to-One ou One-to-One selon vos besoins)
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    // Le token en lui-même (généralement une chaîne UUID ou un JWT signé)
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    // La date et l'heure exactes d'expiration du token
    @Column(nullable = false, name = "expiry_date")
    private Instant expiryDate;

    // --- OPTIONS PROFESSIONNELLES DE SÉCURITÉ ---

    // Permet de révoquer manuellement le token (ex: si l'utilisateur clique sur "Se déconnecter")
    @Column(nullable = false)
    private boolean revoked = false;

    // Permet de savoir si ce token a déjà été utilisé (Utile pour la rotation de tokens)
    @Column(nullable = false)
    private boolean used = false;
}
