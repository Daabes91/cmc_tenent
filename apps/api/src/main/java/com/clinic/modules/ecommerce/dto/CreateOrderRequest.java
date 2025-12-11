package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a new order from cart contents.
 */
public class CreateOrderRequest {

    @NotBlank(message = "Session ID is required")
    @Size(max = 255, message = "Session ID must not exceed 255 characters")
    private String sessionId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    @Size(max = 255, message = "Customer email must not exceed 255 characters")
    private String customerEmail;

    @Size(max = 50, message = "Customer phone must not exceed 50 characters")
    private String customerPhone;

    @NotBlank(message = "Billing address line 1 is required")
    @Size(max = 255, message = "Billing address line 1 must not exceed 255 characters")
    private String billingAddressLine1;

    @Size(max = 255, message = "Billing address line 2 must not exceed 255 characters")
    private String billingAddressLine2;

    @NotBlank(message = "Billing city is required")
    @Size(max = 100, message = "Billing city must not exceed 100 characters")
    private String billingAddressCity;

    @Size(max = 100, message = "Billing state must not exceed 100 characters")
    private String billingAddressState;

    @Size(max = 20, message = "Billing postal code must not exceed 20 characters")
    private String billingAddressPostalCode;

    @NotBlank(message = "Billing country is required")
    @Size(max = 100, message = "Billing country must not exceed 100 characters")
    private String billingAddressCountry;

    private String notes;

    // Constructors
    public CreateOrderRequest() {}

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getBillingAddressLine1() {
        return billingAddressLine1;
    }

    public void setBillingAddressLine1(String billingAddressLine1) {
        this.billingAddressLine1 = billingAddressLine1;
    }

    public String getBillingAddressLine2() {
        return billingAddressLine2;
    }

    public void setBillingAddressLine2(String billingAddressLine2) {
        this.billingAddressLine2 = billingAddressLine2;
    }

    public String getBillingAddressCity() {
        return billingAddressCity;
    }

    public void setBillingAddressCity(String billingAddressCity) {
        this.billingAddressCity = billingAddressCity;
    }

    public String getBillingAddressState() {
        return billingAddressState;
    }

    public void setBillingAddressState(String billingAddressState) {
        this.billingAddressState = billingAddressState;
    }

    public String getBillingAddressPostalCode() {
        return billingAddressPostalCode;
    }

    public void setBillingAddressPostalCode(String billingAddressPostalCode) {
        this.billingAddressPostalCode = billingAddressPostalCode;
    }

    public String getBillingAddressCountry() {
        return billingAddressCountry;
    }

    public void setBillingAddressCountry(String billingAddressCountry) {
        this.billingAddressCountry = billingAddressCountry;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "sessionId='" + sessionId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", billingAddressCity='" + billingAddressCity + '\'' +
                ", billingAddressCountry='" + billingAddressCountry + '\'' +
                '}';
    }
}