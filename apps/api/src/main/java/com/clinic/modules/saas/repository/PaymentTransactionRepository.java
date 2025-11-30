package com.clinic.modules.saas.repository;

import com.clinic.modules.saas.model.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing payment transaction entities.
 * Provides methods to query payment transactions by subscription.
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {

    /**
     * Find all payment transactions for a given subscription.
     *
     * @param subscriptionId the subscription ID
     * @return a list of payment transactions
     */
    List<PaymentTransactionEntity> findBySubscriptionId(Long subscriptionId);

    /**
     * Find a payment transaction by PayPal transaction ID.
     *
     * @param paypalTransactionId the PayPal transaction ID
     * @return an Optional containing the payment transaction if found
     */
    Optional<PaymentTransactionEntity> findByPaypalTransactionId(String paypalTransactionId);
}
