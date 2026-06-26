package com.djeffing.SpecZeta.domain.user.entity;

import com.djeffing.SpecZeta.domain.user.enums.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_provider", columnList = "provider, provider_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 80)
    private String pseudo;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(length = 120)
    private String ville;

    @Column(length = 30)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 150)
    private String providerId;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "rating_moyenne", nullable = false)
    @Builder.Default
    private Double ratingMoyenne = 0.0;

    @Column(name = "nombre_evaluations", nullable = false)
    @Builder.Default
    private Integer nombreEvaluations = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Callback JPA exécuté juste avant le premier INSERT de l'entité.
     * Initialise les timestamps et applique les valeurs par défaut sécuritaires
     * (provider LOCAL, compteurs et booléens à zéro) si elles n'ont pas été renseignées.
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.provider == null) {
            this.provider = AuthProvider.LOCAL;
        }
        if (this.emailVerified == null) {
            this.emailVerified = false;
        }
        if (this.ratingMoyenne == null) {
            this.ratingMoyenne = 0.0;
        }
        if (this.nombreEvaluations == null) {
            this.nombreEvaluations = 0;
        }
    }

    /**
     * Callback JPA exécuté avant chaque UPDATE.
     * Rafraîchit le timestamp {@code updatedAt} pour tracer la dernière modification.
     */
    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
