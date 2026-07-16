package com.djeffing.SpecZeta.domain.annonce.repository;

import com.djeffing.SpecZeta.domain.annonce.entity.FicheTechnique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FicheTechniqueRepository extends JpaRepository<FicheTechnique,Long> {
    @Query("SELECT f FROM FicheTechnique f WHERE f.annonce.id = :annonceId")
    Optional<FicheTechnique> findByAnnoceId(@Param("annonceId") Long annonceId);
}
