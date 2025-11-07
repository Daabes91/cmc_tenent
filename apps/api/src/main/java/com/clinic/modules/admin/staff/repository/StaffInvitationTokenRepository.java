package com.clinic.modules.admin.staff.repository;

import com.clinic.modules.admin.staff.model.StaffInvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffInvitationTokenRepository extends JpaRepository<StaffInvitationToken, Long> {

    /**
     * Find an invitation token by its token string.
     */
    Optional<StaffInvitationToken> findByToken(String token);

    /**
     * Find all invitation tokens for a staff user.
     */
    List<StaffInvitationToken> findByStaffUserId(Long staffUserId);

    /**
     * Find the most recent unused invitation token for a staff user.
     */
    @Query("SELECT t FROM StaffInvitationToken t WHERE t.staffUserId = :staffUserId AND t.usedAt IS NULL ORDER BY t.createdAt DESC")
    Optional<StaffInvitationToken> findLatestUnusedTokenByStaffUserId(Long staffUserId);

    /**
     * Delete expired invitation tokens.
     */
    @Modifying
    @Query("DELETE FROM StaffInvitationToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(Instant now);

    /**
     * Delete all invitation tokens for a staff user.
     */
    void deleteByStaffUserId(Long staffUserId);
}
