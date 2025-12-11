package com.clinic.modules.ecommerce.service;

import com.clinic.api.ApiError;
import com.clinic.modules.ecommerce.exception.EcommerceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service for validating e-commerce operations and data.
 * Provides comprehensive validation for products, orders, payments, and other e-commerce entities.
 */
@Service
public class EcommerceValidationService {

    private static final Logger log = LoggerFactory.getLogger(EcommerceValidationService.class);

    private final Validator validator;

    // Validation patterns
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9_-]{3,50}$");
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]{3,100}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    // Business rule constants
    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;
    private static final BigDecimal MAX_PRICE = new BigDecimal("999999.99");
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 10000;
    private static final int MAX_PRODUCT_NAME_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 5000;

    public EcommerceValidationService(Validator validator) {
        this.validator = validator;
    }

    /**
     * Validate an object using Bean Validation annotations.
     */
    public <T> List<ApiError> validateObject(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
        List<ApiError> errors = new ArrayList<>();

        for (ConstraintViolation<T> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            Object invalidValue = violation.getInvalidValue();
            
            errors.add(ApiError.of(fieldName, message, invalidValue));
        }

        return errors;
    }

    /**
     * Validate product data.
     */
    public List<ApiError> validateProductData(String name, String sku, BigDecimal price, 
                                            String description, String slug) {
        List<ApiError> errors = new ArrayList<>();

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            errors.add(ApiError.of("name", "Product name is required"));
        } else if (name.length() > MAX_PRODUCT_NAME_LENGTH) {
            errors.add(ApiError.of("name", "Product name must not exceed " + MAX_PRODUCT_NAME_LENGTH + " characters"));
        }

        // Validate SKU
        if (sku != null && !sku.trim().isEmpty()) {
            if (!SKU_PATTERN.matcher(sku.trim().toUpperCase()).matches()) {
                errors.add(ApiError.of("sku", "SKU must be 3-50 characters long and contain only letters, numbers, hyphens, and underscores"));
            }
        }

        // Validate price
        if (price != null) {
            if (price.compareTo(MIN_PRICE) < 0) {
                errors.add(ApiError.of("price", "Price must be non-negative"));
            } else if (price.compareTo(MAX_PRICE) > 0) {
                errors.add(ApiError.of("price", "Price must not exceed " + MAX_PRICE));
            }
        }

        // Validate description
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            errors.add(ApiError.of("description", "Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters"));
        }

        // Validate slug
        if (slug != null && !slug.trim().isEmpty()) {
            if (!SLUG_PATTERN.matcher(slug.trim().toLowerCase()).matches()) {
                errors.add(ApiError.of("slug", "Slug must be 3-100 characters long and contain only lowercase letters, numbers, and hyphens"));
            }
        }

        return errors;
    }

    /**
     * Validate cart item data.
     */
    public List<ApiError> validateCartItem(Long productId, Long variantId, Integer quantity) {
        List<ApiError> errors = new ArrayList<>();

        // Validate product ID
        if (productId == null || productId <= 0) {
            errors.add(ApiError.of("productId", "Valid product ID is required"));
        }

        // Validate quantity
        if (quantity == null) {
            errors.add(ApiError.of("quantity", "Quantity is required"));
        } else if (quantity < MIN_QUANTITY) {
            errors.add(ApiError.of("quantity", "Quantity must be at least " + MIN_QUANTITY));
        } else if (quantity > MAX_QUANTITY) {
            errors.add(ApiError.of("quantity", "Quantity must not exceed " + MAX_QUANTITY));
        }

        // Validate variant ID if provided
        if (variantId != null && variantId <= 0) {
            errors.add(ApiError.of("variantId", "Variant ID must be positive if provided"));
        }

        return errors;
    }

    /**
     * Validate customer information for orders.
     */
    public List<ApiError> validateCustomerInfo(String name, String email, String phone, 
                                             String addressLine1, String city, String country) {
        List<ApiError> errors = new ArrayList<>();

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            errors.add(ApiError.of("customerName", "Customer name is required"));
        } else if (name.trim().length() < 2) {
            errors.add(ApiError.of("customerName", "Customer name must be at least 2 characters long"));
        } else if (name.length() > 255) {
            errors.add(ApiError.of("customerName", "Customer name must not exceed 255 characters"));
        }

        // Validate email
        if (email == null || email.trim().isEmpty()) {
            errors.add(ApiError.of("customerEmail", "Customer email is required"));
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.add(ApiError.of("customerEmail", "Invalid email format"));
        }

        // Validate phone (optional but must be valid if provided)
        if (phone != null && !phone.trim().isEmpty()) {
            String cleanPhone = phone.replaceAll("[\\s()-]", "");
            if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                errors.add(ApiError.of("customerPhone", "Invalid phone number format"));
            }
        }

        // Validate address
        if (addressLine1 == null || addressLine1.trim().isEmpty()) {
            errors.add(ApiError.of("billingAddressLine1", "Billing address is required"));
        } else if (addressLine1.length() > 255) {
            errors.add(ApiError.of("billingAddressLine1", "Address line must not exceed 255 characters"));
        }

        // Validate city
        if (city == null || city.trim().isEmpty()) {
            errors.add(ApiError.of("billingAddressCity", "City is required"));
        } else if (city.length() > 100) {
            errors.add(ApiError.of("billingAddressCity", "City must not exceed 100 characters"));
        }

        // Validate country
        if (country == null || country.trim().isEmpty()) {
            errors.add(ApiError.of("billingAddressCountry", "Country is required"));
        } else if (country.length() > 100) {
            errors.add(ApiError.of("billingAddressCountry", "Country must not exceed 100 characters"));
        }

        return errors;
    }

    /**
     * Validate carousel data.
     */
    public List<ApiError> validateCarouselData(String name, String type, String placement, String platform) {
        List<ApiError> errors = new ArrayList<>();

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            errors.add(ApiError.of("name", "Carousel name is required"));
        } else if (name.length() > 255) {
            errors.add(ApiError.of("name", "Carousel name must not exceed 255 characters"));
        }

        // Validate type
        if (type == null || type.trim().isEmpty()) {
            errors.add(ApiError.of("type", "Carousel type is required"));
        } else if (!isValidCarouselType(type)) {
            errors.add(ApiError.of("type", "Invalid carousel type. Must be one of: HERO, PRODUCT, CATEGORY, BRAND, OFFER, TESTIMONIAL, BLOG, MIXED"));
        }

        // Validate placement
        if (placement == null || placement.trim().isEmpty()) {
            errors.add(ApiError.of("placement", "Carousel placement is required"));
        }

        // Validate platform
        if (platform == null || platform.trim().isEmpty()) {
            errors.add(ApiError.of("platform", "Platform is required"));
        } else if (!isValidPlatform(platform)) {
            errors.add(ApiError.of("platform", "Invalid platform. Must be one of: WEB, MOBILE, BOTH"));
        }

        return errors;
    }

    /**
     * Validate payment amount and currency.
     */
    public List<ApiError> validatePaymentData(BigDecimal amount, String currency) {
        List<ApiError> errors = new ArrayList<>();

        // Validate amount
        if (amount == null) {
            errors.add(ApiError.of("amount", "Payment amount is required"));
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(ApiError.of("amount", "Payment amount must be positive"));
        } else if (amount.compareTo(MAX_PRICE) > 0) {
            errors.add(ApiError.of("amount", "Payment amount exceeds maximum allowed"));
        }

        // Validate currency
        if (currency == null || currency.trim().isEmpty()) {
            errors.add(ApiError.of("currency", "Currency is required"));
        } else if (!isValidCurrency(currency)) {
            errors.add(ApiError.of("currency", "Invalid currency code"));
        }

        return errors;
    }

    /**
     * Validate pagination parameters.
     */
    public List<ApiError> validatePaginationParams(Integer page, Integer size) {
        List<ApiError> errors = new ArrayList<>();

        if (page != null && page < 0) {
            errors.add(ApiError.of("page", "Page number must be non-negative"));
        }

        if (size != null) {
            if (size < 1) {
                errors.add(ApiError.of("size", "Page size must be at least 1"));
            } else if (size > 100) {
                errors.add(ApiError.of("size", "Page size must not exceed 100"));
            }
        }

        return errors;
    }

    /**
     * Throw validation exception if errors exist.
     */
    public void throwIfErrors(List<ApiError> errors, String message) {
        if (!errors.isEmpty()) {
            log.warn("Validation failed: {} errors found", errors.size());
            throw new EcommerceException(message + ": " + errors.size() + " validation errors");
        }
    }

    // Helper methods for validation

    private boolean isValidCarouselType(String type) {
        return switch (type.toUpperCase()) {
            case "HERO", "PRODUCT", "CATEGORY", "BRAND", "OFFER", "TESTIMONIAL", "BLOG", "MIXED" -> true;
            default -> false;
        };
    }

    private boolean isValidPlatform(String platform) {
        return switch (platform.toUpperCase()) {
            case "WEB", "MOBILE", "BOTH" -> true;
            default -> false;
        };
    }

    private boolean isValidCurrency(String currency) {
        // Basic currency validation - in production, use a proper currency library
        return switch (currency.toUpperCase()) {
            case "USD", "EUR", "GBP", "CAD", "AUD", "JPY", "CHF", "CNY", "INR", "BRL", "MXN", "AED", "SAR" -> true;
            default -> false;
        };
    }
}