package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing customer orders with billing information and tenant isolation.
 * Contains customer details, billing address, and order totals.
 */
@Entity
@Table(name = "orders", uniqueConstraints = {
    @UniqueConstraint(name = "uk_orders_tenant_order_number", columnNames = {"tenant_id", "order_number"})
})
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull(message = "Tenant is required")
    private TenantEntity tenant;

    @Column(name = "order_number", nullable = false, length = 50)
    @NotBlank(message = "Order number is required")
    @Size(max = 50, message = "Order number must not exceed 50 characters")
    private String orderNumber;

    @Column(name = "customer_name", nullable = false)
    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    @Size(max = 255, message = "Customer email must not exceed 255 characters")
    private String customerEmail;

    @Column(name = "customer_phone", length = 50)
    @Size(max = 50, message = "Customer phone must not exceed 50 characters")
    private String customerPhone;

    @Column(name = "billing_address_line1", nullable = false)
    @NotBlank(message = "Billing address line 1 is required")
    @Size(max = 255, message = "Billing address line 1 must not exceed 255 characters")
    private String billingAddressLine1;

    @Column(name = "billing_address_line2")
    @Size(max = 255, message = "Billing address line 2 must not exceed 255 characters")
    private String billingAddressLine2;

    @Column(name = "billing_address_city", nullable = false, length = 100)
    @NotBlank(message = "Billing city is required")
    @Size(max = 100, message = "Billing city must not exceed 100 characters")
    private String billingAddressCity;

    @Column(name = "billing_address_state", length = 100)
    @Size(max = 100, message = "Billing state must not exceed 100 characters")
    private String billingAddressState;

    @Column(name = "billing_address_postal_code", length = 20)
    @Size(max = 20, message = "Billing postal code must not exceed 20 characters")
    private String billingAddressPostalCode;

    @Column(name = "billing_address_country", nullable = false, length = 100)
    @NotBlank(message = "Billing country is required")
    @Size(max = 100, message = "Billing country must not exceed 100 characters")
    private String billingAddressCountry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @NotNull(message = "Order status is required")
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0", message = "Subtotal must be non-negative")
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Shipping amount must be non-negative")
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount must be non-negative")
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency = "USD";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemEntity> items = new ArrayList<>();

    // Constructors
    public OrderEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public OrderEntity(TenantEntity tenant, String orderNumber) {
        this();
        this.tenant = tenant;
        this.orderNumber = orderNumber;
    }

    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public void calculateTotals() {
        this.subtotal = items.stream()
            .map(OrderItemEntity::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalAmount = subtotal.add(taxAmount).add(shippingAmount);
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
        calculateTotals();
    }

    public void removeItem(OrderItemEntity item) {
        items.remove(item);
        item.setOrder(null);
        calculateTotals();
    }

    public int getTotalItemCount() {
        return items.stream()
            .mapToInt(OrderItemEntity::getQuantity)
            .sum();
    }

    public boolean isPaid() {
        return status == OrderStatus.PAID || 
               status == OrderStatus.PROCESSING || 
               status == OrderStatus.SHIPPED || 
               status == OrderStatus.DELIVERED;
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.PAID;
    }

    public boolean canBeRefunded() {
        return status == OrderStatus.PAID || 
               status == OrderStatus.PROCESSING || 
               status == OrderStatus.SHIPPED || 
               status == OrderStatus.DELIVERED;
    }

    public String getFullBillingAddress() {
        StringBuilder address = new StringBuilder();
        address.append(billingAddressLine1);
        
        if (billingAddressLine2 != null && !billingAddressLine2.trim().isEmpty()) {
            address.append(", ").append(billingAddressLine2);
        }
        
        address.append(", ").append(billingAddressCity);
        
        if (billingAddressState != null && !billingAddressState.trim().isEmpty()) {
            address.append(", ").append(billingAddressState);
        }
        
        if (billingAddressPostalCode != null && !billingAddressPostalCode.trim().isEmpty()) {
            address.append(" ").append(billingAddressPostalCode);
        }
        
        address.append(", ").append(billingAddressCountry);
        
        return address.toString();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEntity> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderEntity)) return false;
        OrderEntity that = (OrderEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OrderEntity{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", currency='" + currency + '\'' +
                ", itemCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}