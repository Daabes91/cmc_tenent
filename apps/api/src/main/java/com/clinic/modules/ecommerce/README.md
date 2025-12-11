# E-commerce Module

This module provides comprehensive e-commerce functionality for the multi-tenant clinic management system.

## Overview

The e-commerce module extends the existing system with:
- Product catalog management
- Shopping cart functionality
- Order processing
- Payment integration (PayPal)
- Content carousels for marketing
- Feature flag system for tenant-level control

## Architecture

The module follows the established patterns in the codebase:
- **Tenant isolation**: All entities include tenant_id and enforce data isolation
- **Service layer**: Business logic separated from controllers
- **Repository pattern**: Data access through Spring Data JPA
- **Exception handling**: Custom exceptions with global error handling
- **Feature flags**: Tenant-level feature enablement

## Module Structure

```
com.clinic.modules.ecommerce/
├── config/           # Configuration and feature flag system
├── controller/       # REST controllers (admin and public APIs)
├── dto/             # Data transfer objects
├── exception/       # Custom exception classes
├── model/           # JPA entities and enums
├── repository/      # Data access layer
└── service/         # Business logic layer
```

## Feature Flag System

E-commerce functionality is controlled by a tenant-level feature flag:
- `tenants.ecommerce_enabled` column controls access
- `EcommerceFeatureService` provides validation methods
- `EcommerceFeatureFilter` automatically validates requests
- `@RequiresEcommerceFeature` annotation for controller methods

## Database Schema

The module adds the following tables:
- `products` - Product catalog with variants support
- `product_variants` - Product variations (size, color, etc.)
- `product_images` - Product images with ordering
- `categories` - Hierarchical product categories
- `product_categories` - Many-to-many product-category relationships
- `carousels` - Content carousels for marketing
- `carousel_items` - Individual carousel content items
- `carts` - Shopping cart sessions
- `cart_items` - Items within shopping carts
- `orders` - Customer orders with billing information
- `order_items` - Items within orders
- `payments` - Payment transaction records

## API Endpoints

### Admin APIs (under `/admin/tenants/{tenantId}/`)
- Product management
- Category management
- Carousel management
- Order management

### Public APIs (under `/public/`)
- Product browsing
- Cart management
- Order creation
- Payment processing
- Carousel display

## Security

- All endpoints enforce tenant isolation
- Feature flag validation on all e-commerce requests
- Existing authentication and authorization patterns
- Payment provider integration security

## Testing

The module includes:
- Unit tests for service layer
- Property-based tests for business logic validation
- Integration tests for end-to-end workflows
- Feature flag validation tests

## Usage

1. Enable e-commerce for a tenant:
   ```java
   ecommerceFeatureService.enableEcommerce(tenantId);
   ```

2. Validate feature access:
   ```java
   @RequiresEcommerceFeature
   @GetMapping("/products")
   public ResponseEntity<?> getProducts(@PathVariable Long tenantId) {
       // Method implementation
   }
   ```

3. Handle e-commerce exceptions:
   ```java
   try {
       ecommerceFeatureService.validateEcommerceEnabled(tenantId);
   } catch (EcommerceFeatureDisabledException e) {
       // Handle feature disabled
   }
   ```

## Migration

The database migration `V20250410_001__create_ecommerce_tables.sql` creates all necessary tables and indexes with proper foreign key constraints and tenant isolation.