package com.clinic.modules.admin.auth;

import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffStatus;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for email content validation.
 * 
 * **Feature: admin-forgot-password, Property 22: Email contains required information**
 * **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**
 * 
 * This test verifies that:
 * 1. For any password reset email sent, the email content includes:
 *    - Clinic name and tenant information
 *    - Clear instructions on how to reset the password
 *    - A prominent call-to-action button with the reset link
 *    - The expiration time of the reset link
 *    - A security notice stating that the link should not be shared
 * 2. Email content is properly formatted for both English and Arabic
 * 3. Email is responsive for mobile devices
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmailContentPropertyTest {

    @Autowired
    private PasswordResetEmailService emailService;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Value("${app.admin-url}")
    private String adminBaseUrl;

    /**
     * Property 22: Email contains required information
     * 
     * For any password reset email sent, the email content should include clinic name,
     * tenant information, reset link, expiration time, and security notice.
     * 
     * **Feature: admin-forgot-password, Property 22: Email contains required information**
     * **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**
     */
    @Test
    public void emailContainsAllRequiredInformation() throws Exception {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Generate random token
                String resetToken = UUID.randomUUID().toString();
                
                // Test both English and Arabic
                String[] languages = {"en", "ar"};
                for (String language : languages) {
                    // Build email content using reflection to access private method
                    String htmlContent = buildEmailContent(
                        resetToken, 
                        tenant.getName(), 
                        tenant.getSlug(), 
                        language
                    );
                    
                    // Verify clinic name is present (Requirement 7.1)
                    assertTrue(htmlContent.contains(tenant.getName()), 
                        String.format("Email should contain clinic name '%s' (language: %s)", 
                            tenant.getName(), language));
                    
                    // Verify tenant slug is present (Requirement 7.1)
                    assertTrue(htmlContent.contains(tenant.getSlug()), 
                        String.format("Email should contain tenant slug '%s' (language: %s)", 
                            tenant.getSlug(), language));
                    
                    // Verify reset link is present (Requirement 7.3)
                    String expectedResetLink = adminBaseUrl + "/reset-password?token=" + resetToken;
                    assertTrue(htmlContent.contains(expectedResetLink), 
                        String.format("Email should contain reset link (language: %s)", language));
                    
                    // Verify expiration time is mentioned (Requirement 7.4)
                    if ("en".equals(language)) {
                        assertTrue(htmlContent.contains("1 hour") || htmlContent.contains("expires"), 
                            "English email should mention expiration time");
                    } else {
                        assertTrue(htmlContent.contains("ساعة") || htmlContent.contains("تنتهي"), 
                            "Arabic email should mention expiration time");
                    }
                    
                    // Verify security notice is present (Requirement 7.5)
                    if ("en".equals(language)) {
                        assertTrue(htmlContent.contains("Security") || htmlContent.contains("not share"), 
                            "English email should contain security notice");
                    } else {
                        assertTrue(htmlContent.contains("أمني") || htmlContent.contains("لا تشارك"), 
                            "Arabic email should contain security notice");
                    }
                    
                    // Verify instructions are present (Requirement 7.2)
                    if ("en".equals(language)) {
                        assertTrue(htmlContent.contains("reset") && htmlContent.contains("password"), 
                            "English email should contain reset instructions");
                    } else {
                        assertTrue(htmlContent.contains("إعادة") && htmlContent.contains("كلمة المرور"), 
                            "Arabic email should contain reset instructions");
                    }
                    
                    // Verify CTA button is present (Requirement 7.3)
                    assertTrue(htmlContent.contains("button") || htmlContent.contains("class=\"button\""), 
                        String.format("Email should contain a button element (language: %s)", language));
                    
                    // Verify HTML structure for mobile responsiveness
                    assertTrue(htmlContent.contains("viewport"), 
                        String.format("Email should have viewport meta tag for mobile (language: %s)", language));
                    assertTrue(htmlContent.contains("@media"), 
                        String.format("Email should have media queries for mobile (language: %s)", language));
                    
                    // Verify proper language attributes
                    if ("ar".equals(language)) {
                        assertTrue(htmlContent.contains("lang=\"ar\""), 
                            "Arabic email should have lang='ar' attribute");
                        assertTrue(htmlContent.contains("dir=\"rtl\""), 
                            "Arabic email should have dir='rtl' attribute");
                    } else {
                        assertTrue(htmlContent.contains("lang=\"en\""), 
                            "English email should have lang='en' attribute");
                    }
                }
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed: " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 22 (variant): Email subject is appropriate for language
     * 
     * For any password reset email, the subject line should be in the correct language.
     * 
     * **Feature: admin-forgot-password, Property 22: Email contains required information**
     * **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**
     */
    @Test
    public void emailSubjectIsInCorrectLanguage() throws Exception {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Test English subject
                String englishSubject = buildSubject("en");
                assertTrue(englishSubject.contains("Reset") || englishSubject.contains("Password"), 
                    "English subject should contain 'Reset' or 'Password'");
                assertFalse(englishSubject.contains("إعادة"), 
                    "English subject should not contain Arabic text");
                
                // Test Arabic subject
                String arabicSubject = buildSubject("ar");
                assertTrue(arabicSubject.contains("إعادة") || arabicSubject.contains("كلمة"), 
                    "Arabic subject should contain Arabic text");
                assertFalse(arabicSubject.contains("Reset"), 
                    "Arabic subject should not contain English text");
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed: " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 22 (variant): Reset link is properly formatted
     * 
     * For any reset token, the generated reset link should be a valid URL.
     * 
     * **Feature: admin-forgot-password, Property 22: Email contains required information**
     * **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**
     */
    @Test
    public void resetLinkIsProperlyFormatted() throws Exception {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Generate random token
                String resetToken = UUID.randomUUID().toString();
                
                // Build reset link using reflection
                String resetLink = buildResetLink(resetToken);
                
                // Verify link starts with admin base URL
                assertTrue(resetLink.startsWith(adminBaseUrl), 
                    "Reset link should start with admin base URL");
                
                // Verify link contains the token
                assertTrue(resetLink.contains(resetToken), 
                    "Reset link should contain the token");
                
                // Verify link has proper format
                assertTrue(resetLink.contains("/reset-password?token="), 
                    "Reset link should have proper format");
                
                // Verify link is a valid URL format
                assertTrue(resetLink.matches("^https?://.*"), 
                    "Reset link should be a valid URL");
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed: " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    // ========== Helper Methods for Accessing Private Methods via Reflection ==========

    private String buildEmailContent(String resetToken, String clinicName, String tenantSlug, String language) 
            throws Exception {
        Method method = PasswordResetEmailService.class.getDeclaredMethod(
            "buildHtmlContent", String.class, String.class, String.class, String.class
        );
        method.setAccessible(true);
        
        String resetLink = adminBaseUrl + "/reset-password?token=" + resetToken;
        return (String) method.invoke(emailService, resetLink, clinicName, tenantSlug, language);
    }

    private String buildSubject(String language) throws Exception {
        Method method = PasswordResetEmailService.class.getDeclaredMethod(
            "buildSubject", String.class
        );
        method.setAccessible(true);
        return (String) method.invoke(emailService, language);
    }

    private String buildResetLink(String token) throws Exception {
        Method method = PasswordResetEmailService.class.getDeclaredMethod(
            "buildResetLink", String.class
        );
        method.setAccessible(true);
        return (String) method.invoke(emailService, token);
    }

    // ========== Helper Methods for Generating Random Test Data ==========

    private TenantEntity createTestTenant() {
        String slug = "tenant-" + UUID.randomUUID().toString().substring(0, 8);
        String name = "Test Clinic " + UUID.randomUUID().toString().substring(0, 8);
        TenantEntity tenant = new TenantEntity(slug, name);
        return tenantRepository.saveAndFlush(tenant);
    }

    private StaffUser createTestStaff(TenantEntity tenant) {
        String email = "staff-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String fullName = "Test Staff " + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = "$2a$10$" + UUID.randomUUID().toString().replace("-", "");
        
        StaffUser staff = new StaffUser(email, fullName, StaffRole.RECEPTIONIST, 
            passwordHash, StaffStatus.ACTIVE);
        staff.setTenant(tenant);
        
        return staffUserRepository.saveAndFlush(staff);
    }
}
