package com.clinic.modules.admin.staff.repository;

import com.clinic.modules.admin.staff.model.StaffUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {
    Optional<StaffUser> findByEmailIgnoreCase(String email);
    Optional<StaffUser> findByDoctorId(Long doctorId);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("""
            select s from StaffUser s
            where lower(coalesce(s.fullName, '')) like lower(concat('%', :term, '%'))
               or lower(s.email) like lower(concat('%', :term, '%'))
            order by s.createdAt desc
            """)
    List<StaffUser> searchStaff(@Param("term") String term, Pageable pageable);
}
