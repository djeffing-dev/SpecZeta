package com.djeffing.SpecZeta.domain.annonce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "certifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "annonce")
public class CertificationBenchmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annonce_id", nullable = false, unique = true)
    private Annonce annonce;

    @Column(name = "type_benchmark", nullable = false, length = 30)
    private String typeBenchmark;

    @Column(name = "url_benchmark", length = 1000)
    private String urlBenchmark;

    @Column(name = "score_monocoeur")
    private Integer scoreMonocoeur;

    @Column(name = "score_multicoeur")
    private Integer scoreMulticoeur;

    @Column(name = "score_gpu")
    private Integer scoreGpu;

    @Column(name = "log_file_url", length = 1000)
    private String logFileUrl;

    @Column(name = "log_data_json", columnDefinition = "TEXT")
    private String logDataJson;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    /**
     * Callback JPA exécuté avant l'INSERT initial.
     * Si la date de vérification n'a pas été fournie explicitement,
     * elle est positionnée à l'instant courant (moment de validation par le service).
     */
    @PrePersist
    void onCreate() {
        if (this.verifiedAt == null) {
            this.verifiedAt = LocalDateTime.now();
        }
    }
}
