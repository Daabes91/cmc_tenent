# E-commerce Error Handling Implementation

## Overview

This document summarizes the comprehensive error handling and validation system implemented for the e-commerce module as part of Task 11.

## Components Implemented

### 1. Structured Error Response DTOs

#### ApiError (`com.clinic.api.ApiError`)
- Represents individual field-level errors
- Includes field name, message, rejected value, error code, and timestamp
- Factory methods for creating different types of errors

#### ApiResponse (`com.clinic.api.ApiResponse`)
- Standard wrapper for all API responses
- Consistent structure for success and error responses
- Includes success flag, data, message, error code, errors list, timestamp, and path

#### ApiResponseFactory (`com.clinic.api.ApiResponseFactory`)
- Factory class for creating standardized API responses
- Convenience methods for common response patterns
- Backward compatibility with existing codebase

### 2. E-commerce Exception Classes

#### Base Exception
- **EcommerceException**: Base class for all e-commerce exceptions

#### Specific Exceptions
- **ProductNotFoundException**: When products/variants are not found
  - Includes productId and tenantId for context
- **InsufficientStockException**: When requested quantity exceeds available stock
  - Includes productId, variantId, requestedQuantity, availableQuantity
- **InvalidCartStateException**: When cart operations fail validation
  - Includes cartId and operation context
- **PaymentProcessingException**: When payment operations fail
  - Includes paymentProvider, orderId, providerErrorCode
- **EcommerceFeatureDisabledException**: When e-commerce feature is not enabled
  - Includes tenantId
- **RateLimitExceededException**: When rate limits are exceeded
  - Includes operation, limit, windowSeconds, retryAfter
- **AuthenticationException**: When authentication fails
  - Includes authenticationType and reason
- **AuthorizationException**: When authorization fails
  - Includes resource, action, userId, tenantId

### 3. Global Exception Handler Updates

#### Enhanced GlobalExceptionHandler
- Added handlers for all new e-commerce exceptions
- Structured error responses with detailed context
- Proper HTTP status codes for each exception type
- Rate limiting headers for rate limit exceptions
- Security logging for authentication/authorization failures

### 4. Validation System

#### EcommerceValidationService
- Comprehensive validation for e-commerce operations
- Product data validation (name, SKU, price, description, slug)
- Cart item validation (productId, variantId, quantity)
- Customer information validation (name, email, phone, address)
- Carousel data validation (name, type, placement, platform)
- Payment data validation (amount, currency)
- Pagination parameter validation

#### Validation Groups
- **EcommerceValidationGroups**: Defines validation contexts
- Different validation rules for different operations
- Support for Bean Validation annotations

### 5. Rate Limiting System

#### RateLimitingConfig
- Configurable rate limits for different operations
- Per-operation limits (cart, order, payment, product browsing, search, admin)
- Configurable time windows and enable/disable flags

#### RateLimitingService
- In-memory rate limiting implementation
- Token bucket algorithm for rate limiting
- Operation-specific rate limits
- Cleanup of expired rate limit data
- Rate limit status reporting

### 6. Transaction Integrity

#### TransactionIntegrityService
- Ensures data consistency during e-commerce operations
- Distributed locking for critical operations
- Cart, order, payment, and stock operation locks
- Tenant-scoped operation isolation
- Transaction management utilities

### 7. Error Logging and Monitoring

#### ErrorLoggingService
- Structured logging for different error types
- Validation, business, security, system, and payment error logging
- MDC (Mapped Diagnostic Context) for structured logs
- Error frequency tracking and alerting
- Security error escalation

## Key Features

### Structured Error Responses
- Consistent JSON structure across all endpoints
- Field-level validation errors with context
- Proper HTTP status codes
- Timestamp and request path information

### Rate Limiting
- Configurable per-operation rate limits
- Proper HTTP 429 responses with retry headers
- Token bucket algorithm implementation
- Memory-efficient with cleanup

### Security
- Authentication and authorization error handling
- Security event logging and monitoring
- Proper error messages without information leakage

### Validation
- Comprehensive input validation
- Business rule validation
- Proper error messages for user feedback

### Transaction Safety
- Distributed locking for critical operations
- Transaction integrity maintenance
- Rollback support for failed operations

## Usage Examples

### Exception Handling
```java
// Throwing a product not found exception
throw new ProductNotFoundException(productId, tenantId);

// Throwing an insufficient stock exception
throw new InsufficientStockException(productId, variantId, 
    requestedQuantity, availableQuantity, "Not enough stock");
```

### Validation
```java
// Validate product data
List<ApiError> errors = validationService.validateProductData(
    name, sku, price, description, slug);
if (!errors.isEmpty()) {
    throw new EcommerceException("Validation failed");
}
```

### Rate Limiting
```java
// Check rate limit before processing
rateLimitingService.checkRateLimit("cart", clientIpAddress);
```

### Transaction Safety
```java
// Execute cart operation with locking
return transactionIntegrityService.executeCartOperation(cartId, () -> {
    // Cart operation logic here
    return result;
});
```

## Testing

### Unit Tests
- **EcommerceExceptionTest**: Tests all exception classes
- **EcommerceValidationServiceTest**: Tests validation logic
- **EcommerceErrorHandlingTest**: Tests global exception handling

### Test Coverage
- Exception constructor and getter methods
- Validation logic for all data types
- Error response structure and content
- Rate limiting behavior
- Transaction integrity

## Configuration

### Application Properties
```properties
# Rate limiting configuration
ecommerce.rate-limiting.enabled=true
ecommerce.rate-limiting.cart-operations-per-minute=60
ecommerce.rate-limiting.order-creation-per-minute=10
ecommerce.rate-limiting.payment-operations-per-minute=5
ecommerce.rate-limiting.product-browsing-per-minute=300
ecommerce.rate-limiting.search-operations-per-minute=100
ecommerce.rate-limiting.admin-operations-per-minute=120
ecommerce.rate-limiting.window-size-seconds=60
```

## Integration with Existing System

### Compatibility
- Maintains backward compatibility with existing API response patterns
- Integrates with existing GlobalExceptionHandler
- Uses existing tenant isolation patterns
- Follows existing logging and monitoring conventions

### Extension Points
- Easy to add new exception types
- Configurable validation rules
- Pluggable rate limiting backends
- Extensible error logging

## Requirements Validation

This implementation addresses all requirements from Task 11:

✅ **9.1**: Structured error responses with field-level details  
✅ **9.2**: Authentication error handling with proper HTTP status codes  
✅ **9.3**: Safe system error responses with detailed logging  
✅ **9.4**: Rate limiting with 429 status and retry information  
✅ **9.5**: Transaction integrity maintenance on validation failure  

## Next Steps

1. **Property-Based Testing**: Implement property-based tests for error handling
2. **Monitoring Integration**: Connect error logging to monitoring systems
3. **Rate Limiting Backend**: Consider Redis for distributed rate limiting
4. **Error Analytics**: Implement error trend analysis and alerting
5. **Documentation**: Create API documentation for error responses