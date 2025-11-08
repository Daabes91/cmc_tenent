package com.clinic.security;

import com.clinic.modules.core.tenant.TenantContext;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class StaffJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(StaffJwtAuthenticationFilter.class);

    private final JwtTokenService staffJwtTokenService;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;

    public StaffJwtAuthenticationFilter(
            StaffJwtTokenService staffJwtTokenService,
            TenantContextHolder tenantContextHolder,
            TenantService tenantService
    ) {
        this.staffJwtTokenService = staffJwtTokenService;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (!path.startsWith("/admin/")) {
            return true;
        }

        // Skip authentication for public auth and setup endpoints
        return path.equals("/admin/auth/login")
            || path.equals("/admin/auth/logout")
            || path.equals("/admin/auth/refresh")
            || path.startsWith("/admin/setup/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Try to get token from Authorization header first
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else {
            // For SSE endpoints, EventSource doesn't support custom headers
            // So we allow token via query parameter as fallback
            String queryToken = request.getParameter("token");
            if (StringUtils.hasText(queryToken)) {
                token = queryToken;
                log.debug("Using query parameter token for SSE endpoint: {}", request.getRequestURI());
            }
        }

        if (token == null) {
            log.debug("Missing Authorization header for admin request {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Staff token required");
            return;
        }

        var principal = tryParseToken(token, request, response);
        if (principal == null) {
            return;
        }

        if (principal.isEmpty()) {
            log.debug("Staff JWT rejected for request {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid staff token");
            return;
        }

        var jwtPrincipal = principal.get();
        var authorities = jwtPrincipal.roles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        var authentication = new UsernamePasswordAuthenticationToken(
                jwtPrincipal,
                null,
                authorities
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Set tenant context from JWT
        if (jwtPrincipal.tenantId() != null) {
            var tenant = tenantService.requireTenant(jwtPrincipal.tenantId());
            tenantContextHolder.setTenant(new TenantContext(tenant.getId(), tenant.getSlug()));
            log.debug("Set tenant context for staff user: tenantId={}, slug={}", tenant.getId(), tenant.getSlug());
        }

        filterChain.doFilter(request, response);
    }

    private java.util.Optional<JwtPrincipal> tryParseToken(String token,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) throws IOException {
        try {
            return staffJwtTokenService.parse(token);
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to parse staff JWT for request {}: {}", request.getRequestURI(), ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid staff token");
            return null;
        } catch (Exception ex) {
            log.error("Unexpected error while validating staff JWT for {}", request.getRequestURI(), ex);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid staff token");
            return null;
        }
    }
}
