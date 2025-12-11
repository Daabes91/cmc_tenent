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
public class PatientJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PatientJwtAuthenticationFilter.class);

    private final JwtTokenService patientJwtTokenService;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;

    public PatientJwtAuthenticationFilter(
            PatientJwtTokenService patientJwtTokenService,
            TenantContextHolder tenantContextHolder,
            TenantService tenantService
    ) {
        this.patientJwtTokenService = patientJwtTokenService;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !(path.startsWith("/public/") || path.startsWith("/api/public/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        var principal = patientJwtTokenService.parse(token);

        if (principal.isEmpty()) {
            log.debug("Patient JWT rejected for request {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid patient token");
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
            log.debug("Set tenant context for patient: tenantId={}, slug={}", tenant.getId(), tenant.getSlug());
        }

        filterChain.doFilter(request, response);
    }
}
