package com.clinic.modules.ecommerce.config;

import com.clinic.modules.ecommerce.service.EcommerceFeatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter that validates e-commerce feature flag for e-commerce endpoints.
 * 
 * This filter intercepts requests to e-commerce endpoints and validates that
 * the e-commerce feature is enabled for the tenant before allowing the request
 * to proceed.
 */
@Component
@Order(100) // Run after authentication but before business logic
public class EcommerceFeatureFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(EcommerceFeatureFilter.class);

    // Patterns for e-commerce endpoints
    private static final Pattern ADMIN_ECOMMERCE_PATTERN = 
            Pattern.compile("^/admin/tenants/(\\d+)/(products|categories|carousels).*");
    private static final Pattern PUBLIC_ECOMMERCE_PATTERN = 
            Pattern.compile("^/public/(products|carousels|cart|orders|payments).*");

    private final EcommerceFeatureService ecommerceFeatureService;
    private final ObjectMapper objectMapper;

    public EcommerceFeatureFilter(EcommerceFeatureService ecommerceFeatureService, 
                                ObjectMapper objectMapper) {
        this.ecommerceFeatureService = ecommerceFeatureService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        log.debug("Processing request: {}", requestURI);

        // Check if this is an e-commerce endpoint
        Long tenantId = extractTenantIdFromEcommerceEndpoint(requestURI);
        
        if (tenantId != null) {
            log.debug("E-commerce endpoint detected for tenant: {}", tenantId);
            
            // Skip validation for public endpoints; controllers will resolve tenant and validate
            if (tenantId >= 0) {
                // Validate e-commerce feature is enabled
                if (!ecommerceFeatureService.isEcommerceEnabled(tenantId)) {
                    log.warn("E-commerce feature access denied for tenant: {} on endpoint: {}", 
                            tenantId, requestURI);
                    sendFeatureDisabledResponse(httpResponse, tenantId);
                    return;
                }
                
                log.debug("E-commerce feature validation passed for tenant: {}", tenantId);
            }
        }

        // Continue with the request
        chain.doFilter(request, response);
    }

    /**
     * Extract tenant ID from e-commerce endpoint URLs.
     * 
     * @param requestURI the request URI
     * @return tenant ID if this is an e-commerce endpoint, null otherwise
     */
    private Long extractTenantIdFromEcommerceEndpoint(String requestURI) {
        // Check admin e-commerce endpoints
        Matcher adminMatcher = ADMIN_ECOMMERCE_PATTERN.matcher(requestURI);
        if (adminMatcher.matches()) {
            try {
                return Long.parseLong(adminMatcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Invalid tenant ID in admin e-commerce endpoint: {}", requestURI);
                return null;
            }
        }

        // Check public e-commerce endpoints
        Matcher publicMatcher = PUBLIC_ECOMMERCE_PATTERN.matcher(requestURI);
        if (publicMatcher.matches()) {
            // For public endpoints, we need to extract tenant from request parameters
            // This will be handled by the tenant resolution logic in the controllers
            // For now, we'll let the request proceed and let the controllers handle validation
            return -1L; // Special marker to indicate public e-commerce endpoint
        }

        return null;
    }

    /**
     * Send a feature disabled error response.
     */
    private void sendFeatureDisabledResponse(HttpServletResponse response, Long tenantId) 
            throws IOException {
        
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = Map.of(
            "error", Map.of(
                "code", "ECOMMERCE_FEATURE_DISABLED",
                "message", "E-commerce feature is not enabled for this tenant",
                "details", Map.of(
                    "tenantId", tenantId,
                    "feature", "ecommerce"
                ),
                "timestamp", Instant.now().toString()
            )
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
