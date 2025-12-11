package com.clinic.modules.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Configuration class for e-commerce module.
 * 
 * This configuration class sets up the e-commerce module and ensures
 * all components are properly scanned and configured.
 */
@Configuration
@ComponentScan(basePackages = "com.clinic.modules.ecommerce")
public class EcommerceConfig {
    
    // Configuration will be added here as needed for:
    // - Payment provider configurations
    // - Cache configurations
    // - Security configurations specific to e-commerce
    // - Feature flag configurations
}