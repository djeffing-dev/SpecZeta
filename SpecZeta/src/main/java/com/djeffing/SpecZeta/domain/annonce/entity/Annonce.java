package com.djeffing.SpecZeta.domain.annonce.entity;

import com.djeffing.SpecZeta.domain.annonce.enums.CategorieAnnonce;
import com.djeffing.SpecZeta.domain.annonce.enums.EtatEsthetique;
import com.djeffing.SpecZeta.domain.annonce.enums.ModeRemise;
import com.djeffing.SpecZeta.domain.annonce.enums.StatutAnnonce;
import com.djeffing.SpecZeta.domain.user.entity.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "annonces",
        indexes = {
                @Index(name = "idx_annonces_statut", columnList = "statut"),
                @Index(name = "idx_annonces_categorie", columnList = "categorie"),
                @Index(name = "idx_annonces_vendeur", columnList = "vendeur_id"),
                @Index(name = "idx_annonces_prix", columnList = "prix"),
                @Index(name = "idx_annonces_created_at", columnList = "created_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"vendeur", "ficheTechnique", "certification", "medias"})
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendeur_id", nullable = false)
    private User vendeur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CategorieAnnonce categorie;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtatEsthetique etat;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_remise", nullable = false, length = 20)
    private ModeRemise modeRemise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutAnnonce statut = StatutAnnonce.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean certifiee = false;

    @OneToOne(mappedBy = "annonce", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FicheTechnique ficheTechnique;

    @OneToOne(mappedBy = "annonce", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CertificationBenchmark certification;

    @OneToMany(mappedBy = "annonce", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordre ASC")
    @Builder.Default
    private List<AnnonceMedia> medias = new ArrayList<>();

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Callback JPA appelé avant l'INSERT initial de l'annonce.
     * Initialise les timestamps, force le statut à {@code ACTIVE}
     * et le flag {@code certifiee} à {@code false} s'ils ne sont pas définis.
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.statut == null) {
            this.statut = StatutAnnonce.ACTIVE;
        }
        if (this.certifiee == null) {
            this.certifiee = false;
        }
    }

    /**
     * Callback JPA exécuté avant chaque UPDATE.
     * Met à jour {@code updatedAt} pour refléter la dernière modification.
     */
    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Ajoute une photo à l'annonce en synchronisant les deux côtés de la relation
     * bidirectionnelle (le média pointe vers cette annonce et la liste est augmentée).
     *
     * @param media la photo à rattacher
     */
    public void addMedia(AnnonceMedia media) {
        media.setAnnonce(this);
        this.medias.add(media);
    }

    /**
     * Retire une photo de l'annonce et coupe le lien inverse côté média
     * pour permettre à {@code orphanRemoval} de supprimer la ligne en base.
     *
     * @param media la photo à détacher
     */
    public void removeMedia(AnnonceMedia media) {
        this.medias.remove(media);
        media.setAnnonce(null);
    }

    /**
     * Rattache la fiche technique à l'annonce dans les deux sens.
     * Utilisé lors de la création initiale ou du remplacement d'une fiche existante.
     *
     * @param fiche la fiche technique générée (TechSpecs/Icecat) ou manuelle
     */
    public void attachFicheTechnique(FicheTechnique fiche) {
        fiche.setAnnonce(this);
        this.ficheTechnique = fiche;
    }

    /**
     * Associe une certification benchmark à l'annonce et passe automatiquement
     * le flag {@code certifiee} à {@code true} pour la mise en avant côté recherche.
     *
     * @param cert le résultat de benchmark validé (Geekbench, 3DMark, CPU-Z…)
     */
    public void attachCertification(CertificationBenchmark cert) {
        cert.setAnnonce(this);
        this.certification = cert;
        this.certifiee = true;
    }
}
