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

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        // Avoid re-resolving tenant during /error dispatches, which can create noisy loops when the DB is unavailable
        return true;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }
        String normalizedPath = path.toLowerCase();
        if (normalizedPath.startsWith("/saas/") || normalizedPath.startsWith("/api/saas/")) {
            return true;
        }

        // Public marketing endpoints are not tenant-specific; skip tenant resolution
        if (normalizedPath.startsWith("/api/public/plans") || normalizedPath.startsWith("/public/plans")) {
            return true;
        }

        // Public signup endpoints (marketing site) should not require tenant resolution
        if (normalizedPath.startsWith("/api/public/signup") || normalizedPath.startsWith("/api/api/public/signup") ||
                normalizedPath.startsWith("/public/signup")) {
            return true;
        }

        // Patient auth endpoints should not require tenant resolution for signup/login
        if (normalizedPath.startsWith("/public/patient/signup") || normalizedPath.startsWith("/public/patient/login") ||
                normalizedPath.startsWith("/api/public/patient/signup") || normalizedPath.startsWith("/api/public/patient/login") ||
                normalizedPath.startsWith("/api/api/public/patient/signup") || normalizedPath.startsWith("/api/api/public/patient/login")) {
            return true;
        }

        // Public payment endpoints should skip tenant resolution
        if (normalizedPath.startsWith("/api/public/payment") || normalizedPath.startsWith("/api/api/public/payment") ||
                normalizedPath.startsWith("/public/payment")) {
            return true;
        }

        // Payment confirmation endpoint should skip tenant resolution
        if (normalizedPath.startsWith("/api/public/payment-confirmation") || normalizedPath.startsWith("/api/api/public/payment-confirmation") ||
                normalizedPath.startsWith("/public/payment-confirmation")) {
            return true;
        }

        return false;
    }

    private TenantEntity resolveTenant(HttpServletRequest request) {
        String slug = resolveSlug(request);
        if (StringUtils.hasText(slug)) {
            return tenantService.requireActiveTenantBySlug(slug);
        }

        String hostHeader = request.getHeader("X-Forwarded-Host");
        String host = StringUtils.hasText(hostHeader) ? hostHeader : request.getServerName();
        String normalizedHost = normalizeHost(host);

        return tenantService.findActiveByDomain(normalizedHost)
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

        return null;
    }

    private String normalizeHost(String host) {
        if (!StringUtils.hasText(host)) {
            return host;
        }

        String cleaned = host.trim().toLowerCase();

        // Remove protocol if present (defensive)
        if (cleaned.startsWith("https://")) {
            cleaned = cleaned.substring(8);
        } else if (cleaned.startsWith("http://")) {
            cleaned = cleaned.substring(7);
        }

        // Drop path if somehow included
        int slashIndex = cleaned.indexOf('/');
        if (slashIndex > -1) {
            cleaned = cleaned.substring(0, slashIndex);
        }

        // Strip port
        int colonIndex = cleaned.indexOf(':');
        if (colonIndex > -1) {
            cleaned = cleaned.substring(0, colonIndex);
        }

        // Strip leading www.
        if (cleaned.startsWith("www.")) {
            cleaned = cleaned.substring(4);
        }

        return cleaned;
    }
}
