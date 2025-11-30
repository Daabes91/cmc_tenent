package com.clinic.modules.core.finance;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new tenant is created.
 * This event triggers automatic seeding of default expense categories.
 */
public class TenantCreatedEvent extends ApplicationEvent {

    private final Long tenantId;
    private final String tenantSlug;

    /**
     * Create a new TenantCreatedEvent.
     *
     * @param source the object on which the event initially occurred
     * @param tenantId the ID of the newly created tenant
     * @param tenantSlug the slug of the newly created tenant
     */
    public TenantCreatedEvent(Object source, Long tenantId, String tenantSlug) {
        super(source);
        this.tenantId = tenantId;
        this.tenantSlug = tenantSlug;
    }

    /**
     * Get the tenant ID.
     *
     * @return the tenant ID
     */
    public Long getTenantId() {
        return tenantId;
    }

    /**
     * Get the tenant slug.
     *
     * @return the tenant slug
     */
    public String getTenantSlug() {
        return tenantSlug;
    }
}
