package com.djeffing.SpecZeta.domain.user.entity;

import com.djeffing.SpecZeta.domain.annonce.entity.Annonce;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_ratings",
        indexes = {
                @Index(name = "idx_ratings_evalue", columnList = "evalue_id"),
                @Index(name = "idx_ratings_evaluateur", columnList = "evaluateur_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rating_per_annonce_evaluateur",
                        columnNames = {"annonce_id", "evaluateur_id"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"evalue", "evaluateur", "annonce"})
public class UserRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evalue_id", nullable = false)
    private User evalue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluateur_id", nullable = false)
    private User evaluateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id")
    private Annonce annonce;

    @Column(nullable = false)
    private Integer note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Callback JPA exécuté avant l'INSERT.
     * Fixe la date de création de l'évaluation à l'instant courant
     * (champ immuable car {@code updatable = false}).
     */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
