package com.clinic.modules.saas.repository;

import com.clinic.modules.saas.model.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing subscription entities.
 * Provides methods to query subscriptions by tenant and PayPal subscription ID.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    /**
     * Find a subscription by tenant ID.
     *
     * @param tenantId the tenant ID
     * @return an Optional containing the subscription if found
     */
    Optional<SubscriptionEntity> findByTenantId(Long tenantId);

    /**
     * Find a subscription by PayPal subscription ID.
     *
     * @param paypalSubscriptionId the PayPal subscription ID
     * @return an Optional containing the subscription if found
     */
    Optional<SubscriptionEntity> findByPaypalSubscriptionId(String paypalSubscriptionId);

    /**
     * Find subscriptions with pending plan tier changes that have reached their effective date.
     * Used by scheduled job to process pending downgrades.
     *
     * @param effectiveDate the date to compare against (typically current date/time)
     * @return list of subscriptions with pending plan changes
     */
    List<SubscriptionEntity> findByPendingPlanTierIsNotNullAndPendingPlanEffectiveDateBefore(LocalDateTime effectiveDate);

    /**
     * Find subscriptions with pending cancellations that have reached their effective date.
     * Used by scheduled job to process scheduled cancellations.
     *
     * @param effectiveDate the date to compare against (typically current date/time)
     * @param status the status to exclude (typically "CANCELLED")
     * @return list of subscriptions with pending cancellations
     */
    List<SubscriptionEntity> findByCancellationEffectiveDateBeforeAndStatusNot(LocalDateTime effectiveDate, String status);

    /**
     * Find all subscriptions by status.
     * Used for cache warming to pre-load active subscriptions.
     *
     * @param status the subscription status (e.g., "ACTIVE", "CANCELLED")
     * @return list of subscriptions with the specified status
     */
    List<SubscriptionEntity> findByStatus(String status);
}
