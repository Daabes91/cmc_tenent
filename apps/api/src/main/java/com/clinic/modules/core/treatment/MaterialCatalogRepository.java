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

    /**
     * Find all materials for a specific tenant
     */
    List<MaterialCatalogEntity> findAllByTenantId(Long tenantId);

    /**
     * Find material by ID and tenant ID
     */
    Optional<MaterialCatalogEntity> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * Find material by name and tenant ID
     */
    Optional<MaterialCatalogEntity> findByTenantIdAndName(Long tenantId, String name);

    /**
     * Search materials by tenant ID and search term
     */
    @Query("""
            select m from MaterialCatalogEntity m
            where m.tenant.id = :tenantId
              and (lower(m.name) like lower(concat('%', :term, '%'))
               or lower(coalesce(m.description, '')) like lower(concat('%', :term, '%'))
               or lower(coalesce(m.unitOfMeasure, '')) like lower(concat('%', :term, '%')))
            order by m.createdAt desc
            """)
    List<MaterialCatalogEntity> searchMaterialsByTenantId(@Param("tenantId") Long tenantId, @Param("term") String term, Pageable pageable);
}
