package com.clinic.security;

import com.clinic.config.SecurityProperties;

public interface JwtVerifier {

    JwtPrincipal verify(String token, SecurityProperties.Token tokenConfig);
}
