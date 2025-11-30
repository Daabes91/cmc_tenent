package com.clinic.modules.core.oauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for OAuth state management
 */
@Repository
public interface OAuthStateRepository extends JpaRepository<OAuthStateEntity, Long> {

    /**
     * Find an OAuth state by its token
     */
    Optional<OAuthStateEntity> findByStateToken(String stateToken);

    /**
     * Find all OAuth states for a specific tenant
     */
    List<OAuthStateEntity> findByTenantSlug(String tenantSlug);

    /**
     * Delete all expired OAuth states
     * This should be called periodically to clean up old states
     */
    @Modifying
    @Query("DELETE FROM OAuthStateEntity o WHERE o.expiresAt < :now")
    int deleteExpiredStates(@Param("now") Instant now);

    /**
     * Delete all consumed OAuth states older than a certain time
     * This helps clean up consumed states that are no longer needed
     */
    @Modifying
    @Query("DELETE FROM OAuthStateEntity o WHERE o.consumed = true AND o.createdAt < :cutoffTime")
    int deleteConsumedStates(@Param("cutoffTime") Instant cutoffTime);
}
