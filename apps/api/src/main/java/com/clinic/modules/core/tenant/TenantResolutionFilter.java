package com.clinic.modules.core.tenant;

import com.clinic.config.TenantProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TenantResolutionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantResolutionFilter.class);

    private final TenantService tenantService;
    private final TenantContextHolder tenantContextHolder;
    private final TenantProperties tenantProperties;

    public TenantResolutionFilter(TenantService tenantService,
                                  TenantContextHolder tenantContextHolder,
                                  TenantProperties tenantProperties) {
        this.tenantService = tenantService;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantProperties = tenantProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            TenantEntity tenant = resolveTenant(request);
            tenantContextHolder.setTenant(new TenantContext(tenant.getId(), tenant.getSlug()));
            filterChain.doFilter(request, response);
        } catch (TenantNotFoundException ex) {
            log.warn("{}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        } finally {
            tenantContextHolder.clear();
        }
    }

    private TenantEntity resolveTenant(HttpServletRequest request) {
        String slug = resolveSlug(request);
        if (StringUtils.hasText(slug)) {
            return tenantService.requireActiveTenantBySlug(slug);
        }

        String hostHeader = request.getHeader("X-Forwarded-Host");
        String host = StringUtils.hasText(hostHeader) ? hostHeader : request.getServerName();

        return tenantService.findActiveByDomain(host)
                .orElseGet(() -> tenantService.requireActiveTenantBySlug(tenantProperties.getDefaultTenantSlug()));
    }

    private String resolveSlug(HttpServletRequest request) {
        String headerValue = request.getHeader(tenantProperties.getHeaderName());
        if (StringUtils.hasText(headerValue)) {
            return headerValue.trim().toLowerCase();
        }

        String paramValue = request.getParameter(tenantProperties.getQueryParameter());
        if (StringUtils.hasText(paramValue)) {
            return paramValue.trim().toLowerCase();
        }

        return tenantProperties.getDefaultTenantSlug();
    }
}
