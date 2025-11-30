package com.clinic.modules.admin.staff.repository;

import com.clinic.modules.admin.staff.model.StaffUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {

    Optional<StaffUser> findByEmailIgnoreCaseAndTenantId(String email, Long tenantId);

    Optional<StaffUser> findByIdAndTenantId(Long id, Long tenantId);

    Optional<StaffUser> findByDoctorIdAndTenantId(Long doctorId, Long tenantId);

    boolean existsByEmailIgnoreCaseAndIdNotAndTenantId(String email, Long id, Long tenantId);

    List<StaffUser> findAllByTenantId(Long tenantId);

    long countByTenantId(Long tenantId);

    @Query("""
            select s from StaffUser s
            where s.tenant.id = :tenantId
              and (
                    lower(coalesce(s.fullName, '')) like lower(concat('%', :term, '%'))
                 or lower(s.email) like lower(concat('%', :term, '%'))
              )
            order by s.createdAt desc
            """)
    List<StaffUser> searchStaff(@Param("tenantId") Long tenantId, @Param("term") String term, Pageable pageable);
}
