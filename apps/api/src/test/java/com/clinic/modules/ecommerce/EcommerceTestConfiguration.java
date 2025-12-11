package com.clinic.modules.ecommerce;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.clinic.modules.ecommerce.service.EcommerceFeatureService;

/**
 * Test configuration to avoid Spring context conflicts in e-commerce tests.
 */
@TestConfiguration
public class EcommerceTestConfiguration {

    @Bean
    @Primary
    public EcommerceFeatureService ecommerceFeatureService() {
        return org.mockito.Mockito.mock(EcommerceFeatureService.class);
    }
}