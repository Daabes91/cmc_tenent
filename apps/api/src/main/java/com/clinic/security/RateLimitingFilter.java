package com.clinic.security;

import com.clinic.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final ConcurrentMap<String, RateState> counters = new ConcurrentHashMap<>();

    public RateLimitingFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Exclude refresh and logout endpoints from rate limiting
        // (they have their own security via refresh tokens)
        if (path.equals("/admin/auth/refresh") || path.equals("/admin/auth/logout")) {
            return true;
        }

        return !(path.startsWith("/public/auth")
                || path.startsWith("/public/book")
                || path.startsWith("/public/bookings")
                || path.startsWith("/public/availability")
                || path.startsWith("/admin/auth"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        SecurityProperties.Bucket config = resolveBucketConfig(path);
        String key = buildRateLimitKey(request);
        if (!allowRequest(key, config)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(config.refillPeriod().toSeconds()));
            response.getWriter().write("{\"error\":\"rate_limited\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String buildRateLimitKey(HttpServletRequest request) {
        String ip = extractClientIp(request);
        String path = request.getServletPath();
        return path + ":" + ip;
    }

    private SecurityProperties.Bucket resolveBucketConfig(String path) {
        if (path.startsWith("/public/auth")) {
            return securityProperties.rateLimiting().publicAuth();
        } else if (path.startsWith("/public/book") || path.startsWith("/public/bookings") || path.startsWith("/public/availability")) {
            return securityProperties.rateLimiting().publicBooking();
        }
        return securityProperties.rateLimiting().adminAuth();
    }

    private boolean allowRequest(String key, SecurityProperties.Bucket config) {
        long windowMillis = config.refillPeriod().toMillis();
        int capacity = Math.max(1, Math.toIntExact(config.capacity()));
        long now = System.currentTimeMillis();

        RateState state = counters.compute(key, (ignored, current) -> {
            if (current == null || now - current.windowStart >= windowMillis) {
                return new RateState(now, 1, false);
            }
            if (current.count >= capacity) {
                return new RateState(current.windowStart, current.count, true);
            }
            return new RateState(current.windowStart, current.count + 1, false);
        });

        return state != null && !state.limited;
    }

    private record RateState(long windowStart, int count, boolean limited) {
    }

    private String extractClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
