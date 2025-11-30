package com.clinic.modules.core.tag;

import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final TenantService tenantService;
    private final TenantContextHolder tenantContextHolder;

    public TagService(TagRepository tagRepository, TenantService tenantService,
            TenantContextHolder tenantContextHolder) {
        this.tagRepository = tagRepository;
        this.tenantService = tenantService;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional(readOnly = true)
    public List<TagEntity> searchTags(String search) {
        Long tenantId = tenantContextHolder.requireTenantId();
        if (search == null || search.isBlank()) {
            return tagRepository.findAllByTenantIdOrderByNameAsc(tenantId);
        }
        return tagRepository.searchByTenantIdAndName(tenantId, search.trim());
    }

    @Transactional
    public TagEntity createOrGetTag(String name, String color) {
        Long tenantId = tenantContextHolder.requireTenantId();
        return tagRepository.findByTenantIdAndNameIgnoreCase(tenantId, name)
                .orElseGet(() -> {
                    TenantEntity tenant = tenantService.requireTenant(tenantId);
                    // TODO: Get actual user ID
                    return tagRepository.save(new TagEntity(tenant, name, color, null));
                });
    }

    @Transactional(readOnly = true)
    public List<TagEntity> findAllByIds(List<Long> ids) {
        return tagRepository.findAllById(ids);
    }
}
