package com.clinic.modules.core.doctor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<DoctorEntity, Long> {

    @Query("select distinct d from DoctorEntity d join d.services s where s.slug = :slug")
    List<DoctorEntity> findAllByServiceSlug(@Param("slug") String slug);

    @Query("select distinct d from DoctorEntity d left join fetch d.services order by d.fullNameEn asc")
    List<DoctorEntity> findAllWithServices();

    @Query("""
            select d from DoctorEntity d
            where lower(coalesce(d.fullNameEn, '')) like lower(concat('%', :term, '%'))
               or lower(coalesce(d.fullNameAr, '')) like lower(concat('%', :term, '%'))
               or lower(coalesce(d.specialtyEn, '')) like lower(concat('%', :term, '%'))
               or lower(coalesce(d.specialtyAr, '')) like lower(concat('%', :term, '%'))
            order by d.createdAt desc
            """)
    List<DoctorEntity> searchDoctors(@Param("term") String term, Pageable pageable);
}
