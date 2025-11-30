package com.clinic.modules.saas.dto;

import com.clinic.modules.saas.model.PlanTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for clinic signup request.
 * Contains all required information to create a new tenant and owner user.
 */
public class SignupRequest {

    @NotBlank(message = "Clinic name is required")
    @Size(min = 2, max = 200, message = "Clinic name must be between 2 and 200 characters")
    private String clinicName;

    @NotBlank(message = "Subdomain is required")
    @Pattern(regexp = "^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?$", 
             message = "Subdomain must be lowercase alphanumeric with hyphens, 2-63 characters")
    @Size(min = 2, max = 63, message = "Subdomain must be between 2 and 63 characters")
    private String subdomain;

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 160, message = "Owner name must be between 2 and 160 characters")
    private String ownerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 160, message = "Email must not exceed 160 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", 
             message = "Phone must be a valid international format (E.164)")
    private String phone;

    @Size(max = 200, message = "Client base URL must be less than 200 characters")
    private String clientBaseUrl;

    @NotNull(message = "Plan tier is required")
    private PlanTier planTier = PlanTier.BASIC;

    @NotBlank(message = "Billing cycle is required")
    @Pattern(regexp = "^(?i)(monthly|annual)$", message = "Billing cycle must be monthly or annual")
    private String billingCycle = "MONTHLY";

    public SignupRequest() {
    }

    public SignupRequest(String clinicName, String subdomain, String ownerName, 
                        String email, String password, String phone) {
        this.clinicName = clinicName;
        this.subdomain = subdomain;
        this.ownerName = ownerName;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    // Getters and Setters

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClientBaseUrl() {
        return clientBaseUrl;
    }

    public void setClientBaseUrl(String clientBaseUrl) {
        this.clientBaseUrl = clientBaseUrl;
    }

    public PlanTier getPlanTier() {
        return planTier;
    }

    public void setPlanTier(PlanTier planTier) {
        this.planTier = planTier;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }
}
