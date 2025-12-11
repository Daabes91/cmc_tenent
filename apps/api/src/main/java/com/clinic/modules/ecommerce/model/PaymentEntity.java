package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity representing payment transactions for e-commerce orders.
 * Tracks PayPal payment processing with tenant isolation.
 */
@Entity(name = "EcommercePayment")
@Table(name = "ecommerce_payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull(message = "Tenant is required")
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order is required")
    private OrderEntity order;

    @Column(name = "provider", nullable = false, length = 50)
    @NotBlank(message = "Payment provider is required")
    @Size(max = 50, message = "Provider must not exceed 50 characters")
    private String provider = "PAYPAL";

    @Column(name = "provider_order_id", length = 255)
    @Size(max = 255, message = "Provider order ID must not exceed 255 characters")
    private String providerOrderId;

    @Column(name = "provider_payment_id", length = 255)
    @Size(max = 255, message = "Provider payment ID must not exceed 255 characters")
    private String providerPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @NotNull(message = "Payment status is required")
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.0", message = "Payment amount must be non-negative")
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency = "USD";

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    // Constructors
    public PaymentEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public PaymentEntity(TenantEntity tenant, OrderEntity order, BigDecimal amount) {
        this();
        this.tenant = tenant;
        this.order = order;
        this.amount = amount;
        this.currency = order.getCurrency();
    }

    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED || status == PaymentStatus.CAPTURED;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED;
    }

    public boolean isPending() {
        return status == PaymentStatus.CREATED || 
               status == PaymentStatus.APPROVED || 
               status == PaymentStatus.PENDING;
    }

    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED || status == PaymentStatus.CAPTURED;
    }

    public void markAsCompleted(String providerPaymentId, String rawResponse) {
        this.status = PaymentStatus.COMPLETED;
        this.providerPaymentId = providerPaymentId;
        this.rawResponse = rawResponse;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed(String rawResponse) {
        this.status = PaymentStatus.FAILED;
        this.rawResponse = rawResponse;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsCaptured(String providerPaymentId, String rawResponse) {
        this.status = PaymentStatus.CAPTURED;
        this.providerPaymentId = providerPaymentId;
        this.rawResponse = rawResponse;
        this.updatedAt = LocalDateTime.now();
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

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderOrderId() {
        return providerOrderId;
    }

    public void setProviderOrderId(String providerOrderId) {
        this.providerOrderId = providerOrderId;
    }

    public String getProviderPaymentId() {
        return providerPaymentId;
    }

    public void setProviderPaymentId(String providerPaymentId) {
        this.providerPaymentId = providerPaymentId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentEntity)) return false;
        PaymentEntity that = (PaymentEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PaymentEntity{" +
                "id=" + id +
                ", provider='" + provider + '\'' +
                ", providerOrderId='" + providerOrderId + '\'' +
                ", providerPaymentId='" + providerPaymentId + '\'' +
                ", status=" + status +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                '}';
    }
}
