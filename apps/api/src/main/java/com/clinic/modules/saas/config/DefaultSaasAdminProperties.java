package com.clinic.modules.saas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the default SAAS administrator account.
 * Allows deployments to control whether the default account is created as well as
 * the email, display name, and initial password.
 */
@Component
@ConfigurationProperties(prefix = "saas.default-admin")
public class DefaultSaasAdminProperties {

    /**
     * Determines whether the initializer should create the default SAAS admin.
     */
    private boolean enabled = true;

    /**
     * Email address used for the default SAAS admin login.
     */
    private String email = "saas.admin@example.com";

    /**
     * Display name for the default administrator.
     */
    private String fullName = "SAAS Platform Admin";

    /**
     * Plain text password that will be hashed on startup.
     * Should be overridden via environment variables in real deployments.
     */
    private String password = "saas-admin";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
