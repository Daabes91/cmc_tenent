package com.clinic.modules.admin.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for password reset functionality
 * All endpoints are public (no authentication required)
 */
@RestController
@RequestMapping("/admin/auth")
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * Request a password reset
     * POST /admin/auth/forgot-password
     * 
     * Returns the same success message regardless of whether the email exists
     * to prevent user enumeration attacks
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        log.info("Password reset requested for email: {} in tenant: {}", 
            request.email(), request.tenantSlug());
        
        try {
            // Extract language code (e.g., "en-US" -> "en")
            String languageCode = language.split("-")[0].toLowerCase();
            if (!languageCode.equals("en") && !languageCode.equals("ar")) {
                languageCode = "en";
            }
            
            passwordResetService.requestPasswordReset(
                request.email(), 
                request.tenantSlug(),
                languageCode
            );
            
            // Always return success message to prevent user enumeration
            return ResponseEntity.ok(Map.of(
                "message", "If there is an account with this email, you will receive a password reset link shortly."
            ));
        } catch (Exception e) {
            // Log the error but don't expose it to the user
            log.error("Error processing password reset request", e);
            
            // Still return success message to prevent user enumeration
            return ResponseEntity.ok(Map.of(
                "message", "If there is an account with this email, you will receive a password reset link shortly."
            ));
        }
    }

    /**
     * Validate a reset token
     * POST /admin/auth/validate-reset-token
     * 
     * Returns whether the token is valid and not expired
     */
    @PostMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Object>> validateResetToken(
            @Valid @RequestBody ValidateTokenRequest request) {
        
        log.info("Token validation requested");
        
        var validationResult = passwordResetService.validateResetTokenWithTenant(request.token());
        
        if (validationResult.isPresent()) {
            var result = validationResult.get();
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "Token is valid",
                "tenantSlug", result.tenantSlug()
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "message", "Token is invalid or expired"
            ));
        }
    }

    /**
     * Reset password using a valid token
     * POST /admin/auth/reset-password
     * 
     * Updates the password and invalidates the token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        log.info("Password reset attempt with token");
        
        boolean success = passwordResetService.resetPassword(
            request.token(), 
            request.newPassword()
        );
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Invalid or expired token"
            ));
        }
    }

    // Request DTOs
    
    public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        
        @NotBlank(message = "Tenant slug is required")
        String tenantSlug
    ) {}
    
    public record ValidateTokenRequest(
        @NotBlank(message = "Token is required")
        String token
    ) {}
    
    public record ResetPasswordRequest(
        @NotBlank(message = "Token is required")
        String token,
        
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword
    ) {}
}
