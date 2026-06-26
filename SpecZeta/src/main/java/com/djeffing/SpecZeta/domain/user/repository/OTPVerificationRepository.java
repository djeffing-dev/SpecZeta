package com.djeffing.SpecZeta.domain.user.repository;

import com.djeffing.SpecZeta.domain.user.entity.OTPVerification;
import com.djeffing.SpecZeta.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPVerificationRepository extends JpaRepository<OTPVerification, Long> {
    @Query("""
            SELECT o FROM OTPVerification o
            WHERE o.user = :user
              AND o.isActive = true
              AND o.isUsed = false
            ORDER BY o.createdAt DESC
            LIMIT 1
            """)
    Optional<OTPVerification> findLatestActiveOtp(@Param("user") User user);

    @Modifying
    @Query("UPDATE OTPVerification o SET o.isActive = false WHERE o.user = :user AND o.isActive = true")
    void invalidateActiveCodes(@Param("user") User user);
}
