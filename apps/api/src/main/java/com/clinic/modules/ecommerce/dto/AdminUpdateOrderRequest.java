package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Admin request for updating an order.
 * All fields are optional; only provided fields will be updated.
 */
public class AdminUpdateOrderRequest {

    @Schema(description = "Customer full name")
    private String customerName;

    @Schema(description = "Customer email")
    private String customerEmail;

    @Schema(description = "Customer phone number")
    private String customerPhone;

    @Schema(description = "Billing address line 1")
    private String billingAddressLine1;

    @Schema(description = "Billing address line 2")
    private String billingAddressLine2;

    @Schema(description = "Billing city")
    private String billingAddressCity;

    @Schema(description = "Billing state")
    private String billingAddressState;

    @Schema(description = "Billing postal code")
    private String billingAddressPostalCode;

    @Schema(description = "Billing country")
    private String billingAddressCountry;

    @Schema(description = "Order status")
    private OrderStatus status;

    @Schema(description = "Subtotal amount")
    private BigDecimal subtotal;

    @Schema(description = "Tax amount")
    private BigDecimal taxAmount;

    @Schema(description = "Shipping amount")
    private BigDecimal shippingAmount;

    @Schema(description = "Total amount")
    private BigDecimal totalAmount;

    @Schema(description = "Order currency (ISO 3)")
    private String currency;

    @Schema(description = "Notes")
    private String notes;

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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
