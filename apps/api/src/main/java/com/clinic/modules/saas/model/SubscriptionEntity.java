package com.clinic.modules.saas.model;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a PayPal subscription for a tenant.
 * Tracks the subscription lifecycle and billing periods.
 */
@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_subscriptions_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_subscriptions_paypal_id", columnList = "paypal_subscription_id"),
    @Index(name = "idx_subscriptions_plan_tier", columnList = "plan_tier"),
    @Index(name = "idx_subscriptions_status", columnList = "status"),
    @Index(name = "idx_subscriptions_renewal_date", columnList = "renewal_date"),
    @Index(name = "idx_subscriptions_pending_effective_date", columnList = "pending_plan_effective_date")
})
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider = "paypal";

    @Column(name = "paypal_subscription_id", unique = true, nullable = false, length = 255)
    private String paypalSubscriptionId;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_tier", length = 50)
    private PlanTier planTier;

    @Column(name = "renewal_date")
    private LocalDateTime renewalDate;

    @Column(name = "payment_method_mask", length = 100)
    private String paymentMethodMask;

    @Column(name = "payment_method_type", length = 50)
    private String paymentMethodType;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Column(name = "cancellation_effective_date")
    private LocalDateTime cancellationEffectiveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "pending_plan_tier", length = 50)
    private PlanTier pendingPlanTier;

    @Column(name = "pending_plan_effective_date")
    private LocalDateTime pendingPlanEffectiveDate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPaypalSubscriptionId() {
        return paypalSubscriptionId;
    }

    public void setPaypalSubscriptionId(String paypalSubscriptionId) {
        this.paypalSubscriptionId = paypalSubscriptionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    public void setCurrentPeriodStart(LocalDateTime currentPeriodStart) {
        this.currentPeriodStart = currentPeriodStart;
    }

    public LocalDateTime getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
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

    public PlanTier getPlanTier() {
        return planTier;
    }

    public void setPlanTier(PlanTier planTier) {
        this.planTier = planTier;
    }

    public LocalDateTime getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(LocalDateTime renewalDate) {
        this.renewalDate = renewalDate;
    }

    public String getPaymentMethodMask() {
        return paymentMethodMask;
    }

    public void setPaymentMethodMask(String paymentMethodMask) {
        this.paymentMethodMask = paymentMethodMask;
    }

    public String getPaymentMethodType() {
        return paymentMethodType;
    }

    public void setPaymentMethodType(String paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }

    public LocalDateTime getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDateTime cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public LocalDateTime getCancellationEffectiveDate() {
        return cancellationEffectiveDate;
    }

    public void setCancellationEffectiveDate(LocalDateTime cancellationEffectiveDate) {
        this.cancellationEffectiveDate = cancellationEffectiveDate;
    }

    public PlanTier getPendingPlanTier() {
        return pendingPlanTier;
    }

    public void setPendingPlanTier(PlanTier pendingPlanTier) {
        this.pendingPlanTier = pendingPlanTier;
    }

    public LocalDateTime getPendingPlanEffectiveDate() {
        return pendingPlanEffectiveDate;
    }

    public void setPendingPlanEffectiveDate(LocalDateTime pendingPlanEffectiveDate) {
        this.pendingPlanEffectiveDate = pendingPlanEffectiveDate;
    }
}
