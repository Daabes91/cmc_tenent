package com.clinic.modules.saas.service;

import com.clinic.modules.saas.dto.BillingAuditLogDto;
import com.clinic.modules.saas.model.BillingAuditLogEntity;
import com.clinic.modules.saas.repository.BillingAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZoneOffset;
@Service
public class BillingAuditLogService {

    private final BillingAuditLogRepository billingAuditLogRepository;
    public BillingAuditLogService(BillingAuditLogRepository billingAuditLogRepository) {
        this.billingAuditLogRepository = billingAuditLogRepository;
    }

    public Page<BillingAuditLogDto> getAuditLogs(Long tenantId, String actionType, Pageable pageable) {
        Specification<BillingAuditLogEntity> spec = Specification.where(null);

        if (tenantId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId));
        }

        if (StringUtils.hasText(actionType)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), actionType.trim()));
        }

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return billingAuditLogRepository.findAll(spec, sortedPageable)
                .map(this::toDto);
    }

    private BillingAuditLogDto toDto(BillingAuditLogEntity entity) {
        BillingAuditLogDto dto = new BillingAuditLogDto();
        dto.setId(entity.getId());
        dto.setTenantId(entity.getTenantId());
        dto.setManagerId(entity.getManagerId());
        dto.setManagerEmail(entity.getManagerEmail());
        dto.setManagerName(entity.getManagerName());
        dto.setAction(entity.getAction());
        dto.setDescription(entity.getDescription());
        dto.setTimestamp(entity.getCreatedAt() != null
                ? entity.getCreatedAt().atZoneSameInstant(ZoneOffset.UTC).toString()
                : null);
        dto.setDetails(entity.getMetadata());
        return dto;
    }
}
