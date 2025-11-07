package com.clinic.modules.core.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface ClinicServiceRepository extends JpaRepository<ClinicServiceEntity, Long> {

    Optional<ClinicServiceEntity> findBySlug(String slug);

    @Query("""
            select s from ClinicServiceEntity s
            where lower(s.nameEn) like lower(concat('%', :term, '%'))
               or lower(coalesce(s.nameAr, '')) like lower(concat('%', :term, '%'))
               or lower(coalesce(s.summaryEn, '')) like lower(concat('%', :term, '%'))
               or lower(coalesce(s.summaryAr, '')) like lower(concat('%', :term, '%'))
            order by s.createdAt desc
            """)
    List<ClinicServiceEntity> searchServices(@Param("term") String term, Pageable pageable);
}
