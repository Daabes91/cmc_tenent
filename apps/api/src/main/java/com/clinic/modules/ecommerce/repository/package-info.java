/**
 * E-commerce data access layer.
 * 
 * This package contains Spring Data JPA repositories for e-commerce entities.
 * All repositories implement tenant-scoped queries and follow established
 * patterns for data access including:
 * - Tenant isolation through @Query annotations
 * - Soft delete support where applicable
 * - Pagination and sorting capabilities
 * - Custom finder methods for business logic
 */
package com.clinic.modules.ecommerce.repository;