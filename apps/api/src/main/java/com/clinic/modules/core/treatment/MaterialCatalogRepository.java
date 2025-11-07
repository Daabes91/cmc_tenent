package com.clinic.modules.core.treatment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialCatalogRepository extends JpaRepository<MaterialCatalogEntity, Long> {

    Optional<MaterialCatalogEntity> findByName(String name);

    List<MaterialCatalogEntity> findByActiveTrue();

    List<MaterialCatalogEntity> findByActiveFalse();

    @Query("""
            select m from MaterialCatalogEntity m
            where lower(m.name) like lower(concat('%', :term, '%'))
               or lower(coalesce(m.description, '')) like lower(concat('%', :term, '%'))
               or lower(coalesce(m.unitOfMeasure, '')) like lower(concat('%', :term, '%'))
            order by m.createdAt desc
            """)
    List<MaterialCatalogEntity> searchMaterials(@Param("term") String term, Pageable pageable);
}
