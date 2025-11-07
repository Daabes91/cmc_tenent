package com.clinic.modules.admin.staff.repository;

import com.clinic.modules.admin.staff.model.StaffRefreshToken;
import com.clinic.modules.admin.staff.model.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StaffRefreshTokenRepository extends JpaRepository<StaffRefreshToken, Long> {

    Optional<StaffRefreshToken> findByToken(String token);

    List<StaffRefreshToken> findAllByUserAndRevokedIsFalse(StaffUser user);

    void deleteAllByExpiresAtBefore(Instant instant);
}
