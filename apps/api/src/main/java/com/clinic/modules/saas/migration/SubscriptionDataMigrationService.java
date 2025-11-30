package com.clinic.modules.saas.migration;

import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import com.clinic.modules.saas.service.PayPalConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for migrating existing subscription data to populate new plan management fields.
 * This is a one-time migration utility to fetch payment method details from PayPal
 * and update subscription records.
 */
@Service
public class SubscriptionDataMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionDataMigrationService.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PayPalConfigService payPalConfigService;
    
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Migrate payment method details from PayPal for all subscriptions.
     * This method fetches subscription details from PayPal API and updates
     * the payment_method_mask and payment_method_type fields.
     * 
     * @return Number of subscriptions successfully updated
     */
    @Transactional
    public int migratePaymentMethodDetails() {
        logger.info("Starting payment method details migration...");
        
        // Find all subscriptions that don't have payment method details
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findAll().stream()
            .filter(s -> s.getPaymentMethodMask() == null || s.getPaymentMethodMask().isEmpty())
            .toList();
        
        logger.info("Found {} subscriptions to migrate", subscriptions.size());
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        subscriptions.forEach(subscription -> {
            try {
                migrateSubscriptionPaymentMethod(subscription);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
                logger.error("Failed to migrate payment method for subscription {}: {}", 
                    subscription.getId(), e.getMessage());
            }
        });
        
        logger.info("Payment method migration completed. Success: {}, Failures: {}", 
            successCount.get(), failureCount.get());
        
        return successCount.get();
    }

    /**
     * Migrate payment method details for a single subscription.
     * 
     * @param subscription The subscription to migrate
     */
    private void migrateSubscriptionPaymentMethod(SubscriptionEntity subscription) {
        try {
            // Fetch subscription details from PayPal
            Map<String, Object> paypalDetails = getSubscriptionDetailsFromPayPal(
                subscription.getPaypalSubscriptionId()
            );
            
            // Extract payment method information
            if (paypalDetails != null) {
                extractAndSetPaymentMethod(subscription, paypalDetails);
            } else {
                // No details available, use default
                subscription.setPaymentMethodType("PAYPAL");
                subscription.setPaymentMethodMask("PayPal Account");
            }
            
            subscriptionRepository.save(subscription);
            logger.debug("Successfully migrated payment method for subscription {}", subscription.getId());
            
        } catch (Exception e) {
            logger.warn("Could not fetch PayPal details for subscription {}, using defaults: {}", 
                subscription.getId(), e.getMessage());
            
            // Set default values if PayPal API call fails
            subscription.setPaymentMethodType("PAYPAL");
            subscription.setPaymentMethodMask("PayPal Account");
            subscriptionRepository.save(subscription);
        }
    }
    
    /**
     * Fetch subscription details from PayPal API.
     * 
     * @param subscriptionId The PayPal subscription ID
     * @return Map containing subscription details
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> getSubscriptionDetailsFromPayPal(String subscriptionId) {
        try {
            String accessToken = payPalConfigService.getAccessToken();
            String baseUrl = payPalConfigService.getBaseUrl();
            
            String url = baseUrl + "/v1/billing/subscriptions/" + subscriptionId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (Map<String, Object>) response.getBody();
            }
            
            return null;
        } catch (Exception e) {
            logger.warn("Failed to fetch subscription details from PayPal for {}: {}", 
                subscriptionId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract payment method information from PayPal subscription data and set on subscription entity.
     * 
     * @param subscription The subscription entity to update
     * @param paypalDetails The PayPal subscription details
     */
    @SuppressWarnings("unchecked")
    private void extractAndSetPaymentMethod(SubscriptionEntity subscription, Map<String, Object> paypalDetails) {
        // Try to extract payment source information
        Object paymentSourceObj = paypalDetails.get("payment_source");
        if (paymentSourceObj instanceof Map) {
            Map<String, Object> paymentSource = (Map<String, Object>) paymentSourceObj;
            
            // Check for card payment
            Object cardObj = paymentSource.get("card");
            if (cardObj instanceof Map) {
                Map<String, Object> card = (Map<String, Object>) cardObj;
                String brand = card.get("brand") != null ? card.get("brand").toString() : "Card";
                String lastDigits = card.get("last_digits") != null ? 
                    card.get("last_digits").toString() : 
                    (card.get("last4") != null ? card.get("last4").toString() : "****");
                
                subscription.setPaymentMethodType("CREDIT_CARD");
                subscription.setPaymentMethodMask(brand + " ****" + lastDigits);
                return;
            }
            
            // Check for PayPal payment
            Object paypalObj = paymentSource.get("paypal");
            if (paypalObj instanceof Map) {
                Map<String, Object> paypal = (Map<String, Object>) paypalObj;
                String email = paypal.get("email_address") != null ? 
                    maskEmail(paypal.get("email_address").toString()) : "PayPal Account";
                
                subscription.setPaymentMethodType("PAYPAL");
                subscription.setPaymentMethodMask(email);
                return;
            }
        }
        
        // Try to extract from subscriber information
        Object subscriberObj = paypalDetails.get("subscriber");
        if (subscriberObj instanceof Map) {
            Map<String, Object> subscriber = (Map<String, Object>) subscriberObj;
            Object emailObj = subscriber.get("email_address");
            if (emailObj != null) {
                subscription.setPaymentMethodType("PAYPAL");
                subscription.setPaymentMethodMask(maskEmail(emailObj.toString()));
                return;
            }
        }
        
        // Default fallback
        subscription.setPaymentMethodType("PAYPAL");
        subscription.setPaymentMethodMask("PayPal Account");
    }

    /**
     * Mask email address for privacy.
     * Example: john.doe@example.com -> j***e@example.com
     * 
     * @param email The email to mask
     * @return Masked email
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "PayPal Account";
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "PayPal Account";
        }
        
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***" + domain;
        }
        
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + domain;
    }

    /**
     * Get migration statistics.
     * 
     * @return Migration statistics as a formatted string
     */
    public String getMigrationStatistics() {
        long totalSubscriptions = subscriptionRepository.count();
        long subscriptionsWithPaymentMethod = subscriptionRepository.findAll().stream()
            .filter(s -> s.getPaymentMethodMask() != null && !s.getPaymentMethodMask().isEmpty())
            .count();
        long subscriptionsWithPlanTier = subscriptionRepository.findAll().stream()
            .filter(s -> s.getPlanTier() != null)
            .count();
        
        return String.format(
            "Migration Statistics:\n" +
            "  Total Subscriptions: %d\n" +
            "  With Payment Method: %d (%.1f%%)\n" +
            "  With Plan Tier: %d (%.1f%%)",
            totalSubscriptions,
            subscriptionsWithPaymentMethod,
            totalSubscriptions > 0 ? (subscriptionsWithPaymentMethod * 100.0 / totalSubscriptions) : 0,
            subscriptionsWithPlanTier,
            totalSubscriptions > 0 ? (subscriptionsWithPlanTier * 100.0 / totalSubscriptions) : 0
        );
    }
}
