package com.clinic.modules.core.patient;

/**
 * Authentication provider types for patient accounts.
 * Defines how a patient can authenticate with the system.
 */
public enum AuthProvider {
    /**
     * Email/password authentication only
     */
    LOCAL,
    
    /**
     * Google OAuth authentication only
     */
    GOOGLE,
    
    /**
     * Both local and Google authentication methods are available
     */
    BOTH
}
