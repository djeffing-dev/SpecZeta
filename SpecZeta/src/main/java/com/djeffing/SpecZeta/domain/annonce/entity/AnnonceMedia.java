package com.djeffing.SpecZeta.domain.annonce.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "annonce_medias",
        indexes = {
                @Index(name = "idx_medias_annonce", columnList = "annonce_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "annonce")
public class AnnonceMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;

    @Column(name = "dropbox_url", nullable = false, length = 1000)
    private String dropboxUrl;

    @Column(name = "dropbox_path", nullable = false, length = 500)
    private String dropboxPath;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordre = 0;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Callback JPA exécuté avant l'INSERT initial.
     * Horodate l'upload et fixe un ordre par défaut à 0 si rien n'est défini.
     */
    @PrePersist
    void onCreate() {
        this.uploadedAt = LocalDateTime.now();
        if (this.ordre == null) {
            this.ordre = 0;
        }
    }
}
