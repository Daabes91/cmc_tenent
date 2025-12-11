/**
 * E-commerce domain models and JPA entities.
 * 
 * This package contains all JPA entities for the e-commerce module including:
 * - Product management (ProductEntity, ProductVariantEntity, ProductImageEntity)
 * - Category management (CategoryEntity, ProductCategoryEntity)
 * - Shopping cart (CartEntity, CartItemEntity)
 * - Order processing (OrderEntity, OrderItemEntity)
 * - Payment tracking (PaymentEntity)
 * - Content management (CarouselEntity, CarouselItemEntity)
 * 
 * All entities follow the established tenant isolation patterns and include
 * proper audit fields and soft delete support where applicable.
 */
package com.clinic.modules.ecommerce.model;