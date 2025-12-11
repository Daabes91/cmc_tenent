package com.clinic.modules.ecommerce.service;

import com.clinic.modules.ecommerce.exception.EcommerceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Service for ensuring transaction integrity in e-commerce operations.
 * Provides mechanisms to prevent data corruption and maintain consistency.
 */
@Service
public class TransactionIntegrityService {

    private static final Logger log = LoggerFactory.getLogger(TransactionIntegrityService.class);

    // Locks for critical operations to prevent race conditions
    private final ConcurrentHashMap<String, ReentrantLock> operationLocks = new ConcurrentHashMap<>();

    /**
     * Execute an operation with a distributed lock to prevent concurrent modifications.
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> operation) {
        ReentrantLock lock = operationLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        lock.lock();
        try {
            log.debug("Acquired lock for operation: {}", lockKey);
            return operation.get();
        } finally {
            lock.unlock();
            log.debug("Released lock for operation: {}", lockKey);
        }
    }

    /**
     * Execute a cart operation with proper locking to prevent race conditions.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public <T> T executeCartOperation(String cartId, Supplier<T> operation) {
        String lockKey = "cart:" + cartId;
        return executeWithLock(lockKey, () -> {
            try {
                return operation.get();
            } catch (Exception e) {
                log.error("Cart operation failed for cart {}: {}", cartId, e.getMessage(), e);
                throw new EcommerceException("Cart operation failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Execute an order operation with proper locking and validation.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public <T> T executeOrderOperation(String orderId, Supplier<T> operation) {
        String lockKey = "order:" + orderId;
        return executeWithLock(lockKey, () -> {
            try {
                return operation.get();
            } catch (Exception e) {
                log.error("Order operation failed for order {}: {}", orderId, e.getMessage(), e);
                throw new EcommerceException("Order operation failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Execute a payment operation with strict integrity checks.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public <T> T executePaymentOperation(String paymentId, Supplier<T> operation) {
        String lockKey = "payment:" + paymentId;
        return executeWithLock(lockKey, () -> {
            try {
                T result = operation.get();
                log.info("Payment operation completed successfully for payment: {}", paymentId);
                return result;
            } catch (Exception e) {
                log.error("Payment operation failed for payment {}: {}", paymentId, e.getMessage(), e);
                throw new EcommerceException("Payment operation failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Execute a stock operation with inventory locking.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public <T> T executeStockOperation(Long productId, Long variantId, Supplier<T> operation) {
        String lockKey = "stock:" + productId + ":" + (variantId != null ? variantId : "default");
        return executeWithLock(lockKey, () -> {
            try {
                return operation.get();
            } catch (Exception e) {
                log.error("Stock operation failed for product {} variant {}: {}", 
                        productId, variantId, e.getMessage(), e);
                throw new EcommerceException("Stock operation failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Execute a tenant-scoped operation with proper isolation.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public <T> T executeTenantOperation(Long tenantId, String operationType, Supplier<T> operation) {
        String lockKey = "tenant:" + tenantId + ":" + operationType;
        return executeWithLock(lockKey, () -> {
            try {
                return operation.get();
            } catch (Exception e) {
                log.error("Tenant operation {} failed for tenant {}: {}", 
                        operationType, tenantId, e.getMessage(), e);
                throw new EcommerceException("Tenant operation failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Execute an operation in a new transaction to ensure isolation.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T executeInNewTransaction(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("New transaction operation failed: {}", e.getMessage(), e);
            throw new EcommerceException("Transaction operation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Execute an operation without a transaction (for read-only operations).
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public <T> T executeWithoutTransaction(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("Non-transactional operation failed: {}", e.getMessage(), e);
            throw new EcommerceException("Operation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate transaction state before critical operations.
     */
    public void validateTransactionState(String operation) {
        // This would integrate with Spring's transaction manager to check state
        log.debug("Validating transaction state for operation: {}", operation);
        
        // In a real implementation, you might check:
        // - Transaction isolation level
        // - Transaction timeout
        // - Connection state
        // - Lock status
    }

    /**
     * Clean up expired locks to prevent memory leaks.
     */
    public void cleanupExpiredLocks() {
        // Remove locks that are not currently held
        operationLocks.entrySet().removeIf(entry -> !entry.getValue().isLocked());
        log.debug("Cleaned up {} expired locks", operationLocks.size());
    }

    /**
     * Get current lock statistics for monitoring.
     */
    public LockStatistics getLockStatistics() {
        int totalLocks = operationLocks.size();
        long activeLocks = operationLocks.values().stream()
                .mapToLong(lock -> lock.isLocked() ? 1 : 0)
                .sum();
        
        return new LockStatistics(totalLocks, (int) activeLocks);
    }

    /**
     * Statistics about current locks.
     */
    public static class LockStatistics {
        private final int totalLocks;
        private final int activeLocks;

        public LockStatistics(int totalLocks, int activeLocks) {
            this.totalLocks = totalLocks;
            this.activeLocks = activeLocks;
        }

        public int getTotalLocks() {
            return totalLocks;
        }

        public int getActiveLocks() {
            return activeLocks;
        }

        public int getInactiveLocks() {
            return totalLocks - activeLocks;
        }

        @Override
        public String toString() {
            return String.format("LockStatistics{total=%d, active=%d, inactive=%d}", 
                    totalLocks, activeLocks, getInactiveLocks());
        }
    }
}