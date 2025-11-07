package com.clinic.security;

import com.clinic.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdminIpFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AdminIpFilter.class);

    private final List<IpAddressMatcher> allowedMatchers;

    public AdminIpFilter(SecurityProperties securityProperties, Environment environment) {
        // Try to get IP allowlist from environment variable first
        String envAllowlist = environment.getProperty("ADMIN_IP_ALLOWLIST");
        List<String> ipList;

        if (envAllowlist != null && !envAllowlist.isBlank()) {
            // Split by comma and trim whitespace
            ipList = java.util.Arrays.stream(envAllowlist.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            log.info("Using IP allowlist from ADMIN_IP_ALLOWLIST environment variable: {}", ipList);
        } else {
            ipList = securityProperties.admin().ipAllowlist();
            log.info("Using IP allowlist from application.yaml: {}", ipList);
        }

        this.allowedMatchers = ipList.stream()
                .map(cidr -> {
                    try {
                        return new IpAddressMatcher(cidr);
                    } catch (Exception e) {
                        log.error("Failed to parse IP CIDR notation: {}. Error: {}", cidr, e.getMessage());
                        throw new IllegalArgumentException("Invalid IP CIDR notation: " + cidr, e);
                    }
                })
                .collect(Collectors.toList());

        if (log.isInfoEnabled()) {
            log.info("Admin IP allowlist configured with {} resulting in {} matchers",
                    ipList, allowedMatchers.size());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/admin/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String remoteAddr = extractClientIp(request);

        if (log.isDebugEnabled()) {
            log.debug("Evaluating admin request from IP {} (path: {}, matchers count: {})",
                    remoteAddr, request.getServletPath(), allowedMatchers.size());
        }

        boolean allowed = allowedMatchers.isEmpty()
                || allowedMatchers.stream().anyMatch(matcher -> matcher.matches(remoteAddr));

        if (!allowed) {
            log.warn("Admin request blocked for IP {} on path {}. Configured IP allowlist has {} matchers.",
                    remoteAddr, request.getServletPath(), allowedMatchers.size());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "IP not allowed");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Admin request from {} allowed", remoteAddr);
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
