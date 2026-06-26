package com.djeffing.SpecZeta.domain.favoris.entity;

import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import com.djeffing.SpecZeta.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "alertes_recherche",
        indexes = {
                @Index(name = "idx_alertes_user", columnList = "user_id"),
                @Index(name = "idx_alertes_active", columnList = "active")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "user")
public class AlerteRecherche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "mots_cles", length = 255)
    private String motsCles;

    @Column(name = "prix_min", precision = 12, scale = 2)
    private BigDecimal prixMin;

    @Column(name = "prix_max", precision = 12, scale = 2)
    private BigDecimal prixMax;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private CategorieAnnonce categorie;

    @Column(length = 120)
    private String localisation;

    @Column(name = "rayon_km")
    private Integer rayonKm;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Callback JPA exécuté avant l'INSERT initial.
     * Initialise les timestamps et active l'alerte par défaut afin qu'elle soit
     * immédiatement prise en compte par le scheduler de matching d'annonces.
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
    }

    /**
     * Callback JPA exécuté avant chaque UPDATE pour tracer la dernière modification
     * (utile pour identifier les alertes désactivées récemment).
     */
    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
