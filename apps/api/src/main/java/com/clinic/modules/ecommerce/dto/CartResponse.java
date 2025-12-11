package com.clinic.modules.ecommerce.dto;

import com.clinic.api.CurrencyFormatter;
import com.clinic.api.TimestampFormatter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for cart information.
 * Used in public cart API responses.
 */
public class CartResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("items")
    private List<CartItemResponse> items;

    @JsonProperty("subtotal")
    private CurrencyFormatter.CurrencyAmount subtotal;

    @JsonProperty("tax_amount")
    private CurrencyFormatter.CurrencyAmount taxAmount;

    @JsonProperty("total_amount")
    private CurrencyFormatter.CurrencyAmount totalAmount;

    @JsonProperty("item_count")
    private Integer itemCount;

    @JsonProperty("total_quantity")
    private Integer totalQuantity;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("expires_at")
    private String expiresAt;

    // Constructors
    public CartResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }

    public CurrencyFormatter.CurrencyAmount getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal, String currency) { 
        this.subtotal = CurrencyFormatter.of(subtotal, currency); 
    }

    public CurrencyFormatter.CurrencyAmount getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount, String currency) { 
        this.taxAmount = CurrencyFormatter.of(taxAmount, currency); 
    }

    public CurrencyFormatter.CurrencyAmount getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount, String currency) { 
        this.totalAmount = CurrencyFormatter.of(totalAmount, currency); 
    }

    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = TimestampFormatter.formatToIso8601(createdAt); 
    }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = TimestampFormatter.formatToIso8601(updatedAt); 
    }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { 
        this.expiresAt = TimestampFormatter.formatToIso8601(expiresAt); 
    }
}