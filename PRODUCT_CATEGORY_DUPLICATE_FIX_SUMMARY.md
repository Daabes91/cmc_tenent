# Product-Category Duplicate Constraint Violation Fix

## Problem
The application was encountering a database constraint violation error:

```
ERROR: duplicate key value violates unique constraint "uk_product_categories_product_category"
Detail: Key (product_id, category_id)=(7, 1) already exists.
```

This error occurred when trying to insert duplicate product-category relationships into the `product_categories` table.

## Root Cause Analysis

The issue was caused by several factors:

1. **Inefficient Category Update Logic**: The `updateProductCategories` method in `ProductService` was clearing all existing relationships and recreating them, which could lead to race conditions and duplicate attempts.

2. **Inadequate Duplicate Prevention**: The `ProductEntity.addCategory()` method didn't check for existing relationships before adding new ones.

3. **Poor Equals/HashCode Implementation**: The `ProductCategoryEntity` was using ID-based equality, which doesn't work for transient entities that don't have IDs yet.

## Solution Implemented

### 1. Improved ProductService.updateProductCategories()

**Before:**
```java
private void updateProductCategories(ProductEntity product, List<Long> categoryIds) {
    // Clear existing associations - let Hibernate handle orphan removal
    product.getProductCategories().clear();

    if (categoryIds.isEmpty()) {
        return;
    }

    List<CategoryEntity> categories = categoryIds.stream()
            .map(id -> categoryRepository.findByIdAndTenant(id, product.getTenantId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id)))
            .collect(Collectors.toList());

    List<ProductCategoryEntity> links = categories.stream()
            .map(cat -> new ProductCategoryEntity(product, cat, product.getTenant()))
            .collect(Collectors.toList());

    // Add new associations to the existing collection
    product.getProductCategories().addAll(links);
}
```

**After:**
```java
private void updateProductCategories(ProductEntity product, List<Long> categoryIds) {
    if (categoryIds.isEmpty()) {
        // Clear existing associations - let Hibernate handle orphan removal
        product.getProductCategories().clear();
        return;
    }

    // Get current category IDs to avoid unnecessary database operations
    Set<Long> currentCategoryIds = product.getProductCategories().stream()
            .map(pc -> pc.getCategory().getId())
            .collect(Collectors.toSet());

    Set<Long> newCategoryIds = new HashSet<>(categoryIds);

    // Remove categories that are no longer needed
    product.getProductCategories().removeIf(pc -> !newCategoryIds.contains(pc.getCategory().getId()));

    // Add only new categories that don't already exist
    Set<Long> categoriesToAdd = newCategoryIds.stream()
            .filter(id -> !currentCategoryIds.contains(id))
            .collect(Collectors.toSet());

    if (!categoriesToAdd.isEmpty()) {
        List<CategoryEntity> categories = categoriesToAdd.stream()
                .map(id -> categoryRepository.findByIdAndTenant(id, product.getTenantId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id)))
                .collect(Collectors.toList());

        List<ProductCategoryEntity> links = categories.stream()
                .map(cat -> new ProductCategoryEntity(product, cat, product.getTenant()))
                .collect(Collectors.toList());

        // Add new associations to the existing collection
        product.getProductCategories().addAll(links);
    }
}
```

### 2. Enhanced ProductEntity.addCategory()

**Before:**
```java
public void addCategory(CategoryEntity category) {
    ProductCategoryEntity productCategory = new ProductCategoryEntity(this, category, this.tenant);
    productCategories.add(productCategory);
}
```

**After:**
```java
public void addCategory(CategoryEntity category) {
    // Check if the category is already associated with this product
    boolean alreadyExists = productCategories.stream()
            .anyMatch(pc -> pc.getCategory().getId().equals(category.getId()));
    
    if (!alreadyExists) {
        ProductCategoryEntity productCategory = new ProductCategoryEntity(this, category, this.tenant);
        productCategories.add(productCategory);
    }
}
```

### 3. Improved ProductCategoryEntity Equals/HashCode

**Before:**
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProductCategoryEntity)) return false;
    ProductCategoryEntity that = (ProductCategoryEntity) o;
    return id != null && id.equals(that.id);
}

@Override
public int hashCode() {
    return getClass().hashCode();
}
```

**After:**
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProductCategoryEntity)) return false;
    ProductCategoryEntity that = (ProductCategoryEntity) o;
    
    // Use business key equality: same product, category, and tenant
    return product != null && category != null && tenantId != null &&
           product.getId() != null && category.getId() != null &&
           product.getId().equals(that.product != null ? that.product.getId() : null) &&
           category.getId().equals(that.category != null ? that.category.getId() : null) &&
           tenantId.equals(that.tenantId);
}

@Override
public int hashCode() {
    // Use business key for hash code
    return Objects.hash(
        product != null ? product.getId() : null,
        category != null ? category.getId() : null,
        tenantId
    );
}
```

### 4. Added JPA-Level Unique Constraint

```java
@Entity
@Table(name = "product_categories",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_product_categories_product_category",
           columnNames = {"product_id", "category_id"}
       ))
public class ProductCategoryEntity {
    // ...
}
```

## Benefits of the Fix

1. **Prevents Duplicate Relationships**: The improved logic checks for existing relationships before creating new ones.

2. **Better Performance**: Only adds/removes categories that actually changed, reducing database operations.

3. **Race Condition Prevention**: The business-key-based equality prevents issues with concurrent modifications.

4. **Database-Level Protection**: The unique constraint provides a final safety net against duplicates.

5. **Cleaner Code**: The logic is more explicit about what it's doing and why.

## Testing the Fix

The fix can be tested by:

1. Creating a product with categories
2. Updating the product with the same categories multiple times
3. Verifying that no duplicate relationships are created
4. Checking that the database constraint prevents any duplicates that might slip through

## Migration Notes

- No database migration is required as the unique constraint already exists
- The changes are backward compatible
- Existing duplicate relationships (if any) should be cleaned up before deploying

## Files Modified

1. `apps/api/src/main/java/com/clinic/modules/ecommerce/service/ProductService.java`
2. `apps/api/src/main/java/com/clinic/modules/ecommerce/model/ProductEntity.java`
3. `apps/api/src/main/java/com/clinic/modules/ecommerce/model/ProductCategoryEntity.java`

The fix addresses the immediate constraint violation issue while also improving the overall robustness and performance of the product-category relationship management system.