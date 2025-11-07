package com.clinic.modules.core.tenant;

import com.clinic.config.TenantProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    private final TenantService tenantService;
    private final TenantProperties tenantProperties;

    public TenantContextHolder(TenantService tenantService, TenantProperties tenantProperties) {
        this.tenantService = tenantService;
        this.tenantProperties = tenantProperties;
    }

    public void setTenant(TenantContext context) {
        Assert.notNull(context, "Tenant context must not be null");
        CONTEXT.set(context);
    }

    public TenantContext getTenant() {
        TenantContext context = CONTEXT.get();
        if (context == null) {
            TenantEntity tenant = tenantService.requireActiveTenantBySlug(tenantProperties.getDefaultTenantSlug());
            context = new TenantContext(tenant.getId(), tenant.getSlug());
            CONTEXT.set(context);
        }
        return context;
    }

    public Long requireTenantId() {
        return getTenant().tenantId();
    }

    public String requireTenantSlug() {
        return getTenant().slug();
    }

    public void clear() {
        CONTEXT.remove();
    }
}
