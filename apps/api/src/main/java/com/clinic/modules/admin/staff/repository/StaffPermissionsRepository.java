package com.clinic.modules.admin.staff.repository;

import com.clinic.modules.admin.staff.model.StaffPermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffPermissionsRepository extends JpaRepository<StaffPermissions, Long> {

    /**
     * Find permissions by staff user ID.
     */
    Optional<StaffPermissions> findByStaffUserId(Long staffUserId);

    /**
     * Check if permissions exist for a staff user.
     */
    boolean existsByStaffUserId(Long staffUserId);

    /**
     * Delete permissions by staff user ID.
     */
    void deleteByStaffUserId(Long staffUserId);
}
