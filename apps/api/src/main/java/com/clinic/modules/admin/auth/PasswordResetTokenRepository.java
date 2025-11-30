package com.clinic.modules.admin.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    /**
     * Find a password reset token by its hashed value
     */
    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    /**
     * Find all unused tokens for a specific staff member
     */
    @Query("SELECT t FROM PasswordResetTokenEntity t WHERE t.staff.id = :staffId AND t.used = false")
    List<PasswordResetTokenEntity> findByStaffIdAndUsedFalse(@Param("staffId") Long staffId);

    /**
     * Invalidate all existing unused tokens for a staff member
     */
    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true, t.usedAt = :usedAt WHERE t.staff.id = :staffId AND t.used = false")
    void invalidateTokensForStaff(@Param("staffId") Long staffId, @Param("usedAt") Instant usedAt);

    /**
     * Delete expired tokens (for cleanup job)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :expirationThreshold")
    void deleteExpiredTokens(@Param("expirationThreshold") Instant expirationThreshold);

    /**
     * Count unused tokens for a staff member (useful for rate limiting checks)
     */
    @Query("SELECT COUNT(t) FROM PasswordResetTokenEntity t WHERE t.staff.id = :staffId AND t.used = false AND t.expiresAt > :now")
    long countActiveTokensForStaff(@Param("staffId") Long staffId, @Param("now") Instant now);
}
