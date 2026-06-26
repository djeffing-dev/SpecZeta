package com.djeffing.SpecZeta.domain.annonce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "fiches_techniques")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "annonce")
public class FicheTechnique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annonce_id", nullable = false, unique = true)
    private Annonce annonce;

    @Column(length = 150)
    private String modele;

    @Column(length = 100)
    private String marque;

    @Column(length = 200)
    private String processeur;

    @Column(length = 200)
    private String gpu;

    @Column(name = "ram_go")
    private Integer ramGo;

    @Column(name = "stockage_go")
    private Integer stockageGo;

    @Column(name = "type_stockage", length = 30)
    private String typeStockage;

    @Column(name = "ecran_taille", length = 30)
    private String ecranTaille;

    @Column(name = "ecran_resolution", length = 30)
    private String ecranResolution;

    @Column(length = 50)
    private String socket;

    @Column(name = "source_api", length = 30)
    private String sourceApi;

    @Column(name = "raw_data_json", columnDefinition = "TEXT")
    private String rawDataJson;
}
