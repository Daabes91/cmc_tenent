package com.clinic.security;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantContext;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.saas.service.BillingAccessControlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter to check billing status for tenant admin requests.
 * Blocks access to tenant admin panel if billing status is not ACTIVE.
 * Allows access to login and billing-related pages regardless of status.
 */
@Component
public class BillingStatusFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(BillingStatusFilter.class);

    private final BillingAccessControlService billingAccessControlService;
    private final TenantContextHolder tenantContextHolder;
    private final ObjectMapper objectMapper;

    public BillingStatusFilter(
            BillingAccessControlService billingAccessControlService,
            TenantContextHolder tenantContextHolder,
            ObjectMapper objectMapper) {
        this.billingAccessControlService = billingAccessControlService;
        this.tenantContextHolder = tenantContextHolder;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        
        // Only apply to /admin/** endpoints
        if (!path.startsWith("/admin/")) {
            return true;
        }

        // Allow access to authentication endpoints
        if (path.startsWith("/admin/auth/")
            || path.startsWith("/admin/setup/")) {
            return true;
        }

        // Allow access to billing-related pages
        if (path.startsWith("/admin/billing/")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Get tenant context
        TenantContext tenantContext = tenantContextHolder.getTenant();
        
        if (tenantContext == null) {
            logger.warn("No tenant context found for billing status check on path: {}", request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        Long tenantId = tenantContext.tenantId();
        
        // Check billing status
        BillingStatus billingStatus = billingAccessControlService.getBillingStatus(tenantId);
        
        if (billingStatus == BillingStatus.ACTIVE) {
            // Allow access for active billing
            filterChain.doFilter(request, response);
            return;
        }

        // Block access for non-active billing status
        logger.info("Access denied for tenant {} due to billing status: {}", tenantId, billingStatus);
        
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Access Denied");
        errorResponse.put("billingStatus", billingStatus.name());
        errorResponse.put("message", getBillingStatusMessage(billingStatus));
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Get user-friendly message based on billing status.
     *
     * @param status the billing status
     * @return appropriate message for the status
     */
    private String getBillingStatusMessage(BillingStatus status) {
        return switch (status) {
            case PENDING_PAYMENT -> 
                "Please complete payment to activate your account and access the admin panel.";
            case PAST_DUE -> 
                "Your payment is past due. Please update your payment method to restore access.";
            case CANCELED -> 
                "Your subscription has been canceled. Please reactivate your subscription to continue using the platform.";
            default -> 
                "Access to the admin panel is currently restricted due to billing status.";
        };
    }
}
