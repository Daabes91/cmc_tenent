package com.clinic.security;

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
public class SaasManagerJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SaasManagerJwtAuthenticationFilter.class);

    private final SaasManagerJwtTokenService saasManagerJwtTokenService;

    public SaasManagerJwtAuthenticationFilter(SaasManagerJwtTokenService saasManagerJwtTokenService) {
        this.saasManagerJwtTokenService = saasManagerJwtTokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Only apply this filter to /saas/** paths
        if (!path.startsWith("/saas/")) {
            return true;
        }

        // Skip authentication for public auth endpoints
        return path.equals("/saas/auth/login") || path.equals("/saas/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // Extract JWT from Authorization header
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (token == null) {
            log.warn("SAAS Manager authentication failed - missing token - method: {}, uri: {}", method, requestUri);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "SAAS Manager token required");
            return;
        }

        var principal = tryParseToken(token, request, response);
        if (principal == null) {
            return;
        }

        if (principal.isEmpty()) {
            log.warn("SAAS Manager authentication failed - invalid token - method: {}, uri: {}", method, requestUri);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid SAAS Manager token");
            return;
        }

        var jwtPrincipal = principal.get();
        
        // Verify that the token has SAAS_MANAGER audience
        if (jwtPrincipal.audience() != JwtAudience.SAAS_MANAGER) {
            log.warn("SAAS Manager authentication failed - wrong audience: {} - method: {}, uri: {}", 
                    jwtPrincipal.audience(), method, requestUri);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token audience");
            return;
        }

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
        
        log.debug("SAAS Manager authenticated successfully - subject: {}, roles: {}, method: {}, uri: {}", 
                jwtPrincipal.subject(), jwtPrincipal.roles(), method, requestUri);
        
        filterChain.doFilter(request, response);
    }

    private java.util.Optional<JwtPrincipal> tryParseToken(String token,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) throws IOException {
        try {
            return saasManagerJwtTokenService.parse(token);
        } catch (IllegalArgumentException ex) {
            log.warn("SAAS Manager JWT parsing failed - method: {}, uri: {}, error: {}", 
                    request.getMethod(), request.getRequestURI(), ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid SAAS Manager token");
            return null;
        } catch (Exception ex) {
            log.error("SAAS Manager JWT validation error - method: {}, uri: {}, error: {}", 
                    request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid SAAS Manager token");
            return null;
        }
    }
}
