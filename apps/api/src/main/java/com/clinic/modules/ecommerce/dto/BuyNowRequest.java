package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for direct product purchase (Buy Now functionality).
 * Bypasses cart and creates order directly from product selection.
 */
public class BuyNowRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    private Long variantId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    private String customerPhone;

    @NotBlank(message = "Billing address line 1 is required")
    private String billingAddressLine1;

    private String billingAddressLine2;

    @NotBlank(message = "Billing city is required")
    private String billingAddressCity;

    private String billingAddressState;

    private String billingAddressPostalCode;

    @NotBlank(message = "Billing country is required")
    private String billingAddressCountry;

    private String notes;

    // Constructors
    public BuyNowRequest() {}

    public BuyNowRequest(Long productId, Integer quantity, String customerName, String customerEmail,
                        String billingAddressLine1, String billingAddressCity, String billingAddressCountry) {
        this.productId = productId;
        this.quantity = quantity;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.billingAddressLine1 = billingAddressLine1;
        this.billingAddressCity = billingAddressCity;
        this.billingAddressCountry = billingAddressCountry;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
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
        return "BuyNowRequest{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", variantId=" + variantId +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", billingAddressLine1='" + billingAddressLine1 + '\'' +
                ", billingAddressLine2='" + billingAddressLine2 + '\'' +
                ", billingAddressCity='" + billingAddressCity + '\'' +
                ", billingAddressState='" + billingAddressState + '\'' +
                ", billingAddressPostalCode='" + billingAddressPostalCode + '\'' +
                ", billingAddressCountry='" + billingAddressCountry + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}