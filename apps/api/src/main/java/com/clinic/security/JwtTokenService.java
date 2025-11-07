package com.clinic.security;

import java.util.Optional;

public interface JwtTokenService {

    Optional<JwtPrincipal> parse(String token);
}

