package com.clinic.modules.core.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {

    @Query("SELECT t FROM TagEntity t WHERE t.tenant.id = :tenantId AND lower(t.name) = lower(:name)")
    Optional<TagEntity> findByTenantIdAndNameIgnoreCase(Long tenantId, String name);

    @Query("SELECT t FROM TagEntity t WHERE t.tenant.id = :tenantId AND lower(t.name) LIKE lower(concat('%', :search, '%')) ORDER BY t.name ASC")
    List<TagEntity> searchByTenantIdAndName(Long tenantId, String search);

    List<TagEntity> findAllByTenantIdOrderByNameAsc(Long tenantId);
}
