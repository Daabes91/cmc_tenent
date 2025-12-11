package com.clinic.modules.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for updating customer email in cart.
 * Used in public cart API endpoints.
 */
public class UpdateCustomerEmailRequest {

    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    // Constructors
    public UpdateCustomerEmailRequest() {}

    public UpdateCustomerEmailRequest(String email) {
        this.email = email;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "UpdateCustomerEmailRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}