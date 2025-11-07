package com.clinic.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PemUtilsLoadsKeys {
    @Test
    void loadsStaffPublicKeyFromClasspath() {
        String pem = PemUtils.loadPem("classpath:keys/staff_public.pem");
        assertThat(pem).contains("-----BEGIN PUBLIC KEY-----");
    }
}
