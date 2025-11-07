package com.clinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "multitenant")
public class TenantProperties {

    private String defaultTenantSlug = "default";
    private String headerName = "X-Tenant-Slug";
    private String queryParameter = "tenant";

    public String getDefaultTenantSlug() {
        return defaultTenantSlug;
    }

    public void setDefaultTenantSlug(String defaultTenantSlug) {
        this.defaultTenantSlug = defaultTenantSlug;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getQueryParameter() {
        return queryParameter;
    }

    public void setQueryParameter(String queryParameter) {
        this.queryParameter = queryParameter;
    }
}
