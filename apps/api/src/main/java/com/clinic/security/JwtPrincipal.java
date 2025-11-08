package com.clinic.security;

import java.util.List;

public record JwtPrincipal(
        String subject,
        JwtAudience audience,
        List<String> roles,
        Long tenantId
) {
}

