# Product Update Hibernate Collection Management Fix

## Problem
When updating a product with category changes, the system was throwing Hibernate exceptions:

1. **Initial Issue**: `UnsupportedOperationException` when using immutable collections
2. **Secondary Issue**: `HibernateException: A collection with cascade="all-delete-orphan" was no longer referenced by the owning entity instance`

## Root Cause
The `ProductService.updateProductCategories()` method had two issues:

1. **Immutable Collection**: Using `List.of()` created immutable collections that Hibernate couldn't manage
2. **Collection Reference Replacement**: Calling `setProductCategories(new ArrayList<>())` replaced the collection reference that Hibernate was tracking for orphan removal

## Solution
Completely refactored the collection management approach:

### Before (Problematic):
```java
private void updateProductCategories(ProductEntity product, List<Long> categoryIds) {
    if (product.getProductCategories() != null) {
        product.getProductCategories().clear();
    }
    productCategoryRepository.deleteByTenantAndProduct(product.getTenantId(), product.getId());

    if (categoryIds.isEmpty()) {
        product.setProductCategories(List.of()); // ❌ Immutable + Reference replacement
        return;
    }
    
    // ... create new entities
    productCategoryRepository.saveAll(links); // ❌ Manual save conflicts with cascade
    product.setProductCategories(new ArrayList<>(links)); // ❌ Reference replacement
}
```

### After (Fixed):
```java
private void updateProductCategories(ProductEntity product, List<Long> categoryIds) {
    // Clear existing associations - let Hibernate handle orphan removal
    product.getProductCategories().clear();

    if (categoryIds.isEmpty()) {
        // Collection is already cleared, no need to set a new reference
        return;
    }

    // ... create new entities
    
    // Add new associations to the existing collection (don't replace the collection reference)
    product.getProductCategories().addAll(links);
}
```

## Key Changes
1. **Removed collection reference replacement**: Never call `setProductCategories()` with a new collection
2. **Removed manual database operations**: Let Hibernate's `CascadeType.ALL` and `orphanRemoval = true` handle persistence
3. **Use collection modification methods**: Only use `clear()` and `addAll()` on the existing collection

## Files Changed
- `apps/api/src/main/java/com/clinic/modules/ecommerce/service/ProductService.java`
  - Refactored `updateProductCategories()` method
  - Removed manual `productCategoryRepository.deleteByTenantAndProduct()` call
  - Removed manual `productCategoryRepository.saveAll()` call
  - Replaced collection reference replacement with `addAll()`

## Testing
The fix ensures that:
1. Hibernate properly tracks the collection for orphan removal
2. Empty category lists are handled correctly
3. New category associations are properly persisted via cascade
4. No collection reference conflicts occur during entity merging

## Impact
This fix resolves both the immutable collection issue and the Hibernate orphan removal conflict, allowing users to successfully update product categories including clearing all categories from a product.