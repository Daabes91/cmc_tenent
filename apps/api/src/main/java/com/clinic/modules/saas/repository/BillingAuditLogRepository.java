package com.clinic.modules.saas.repository;

import com.clinic.modules.saas.model.BillingAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingAuditLogRepository extends JpaRepository<BillingAuditLogEntity, Long>,
        JpaSpecificationExecutor<BillingAuditLogEntity> {
}
