# Product Image Fix Test

## Issue
Product images were not displaying because the `ProductCard` component was incorrectly accessing the image data.

## Root Cause
The component was trying to use `product.images[0]` directly as a URL string, but `product.images` is an array of `ProductImage` objects with this structure:
```json
{
  "id": 3,
  "imageUrl": "https://imagedelivery.net/...",
  "altText": "Product - Main Image",
  "sortOrder": 1,
  "isMain": true
}
```

## Fix Applied
Updated the image selection logic in `ProductCard.tsx` to:
1. First look for an image with `isMain: true`
2. If no main image found, use the first image in the array
3. Extract the `imageUrl` property from the image object
4. Fall back to `mainImageUrl` for backward compatibility

## New Logic
```typescript
const image = useMemo(() => {
  // First try to find the main image
  if (product.images && product.images.length > 0) {
    const mainImage = product.images.find(img => img.isMain);
    if (mainImage) {
      return mainImage.imageUrl;
    }
    // If no main image, use the first image
    return product.images[0].imageUrl;
  }
  // Fallback to mainImageUrl if it exists (for backward compatibility)
  return product.mainImageUrl || undefined;
}, [product.images, product.mainImageUrl]);
```

## Expected Result
- Products with `isMain: true` images will display that image
- Products without a main image will display the first image in the array
- Products with no images will show the placeholder
- The fix works with the existing API response structure

## Test Cases
Based on your API response:

1. **Product ID 7 (aaaaa)**: Has 1 image with `isMain: true` → Should display the image
2. **Product ID 6 (mossss)**: Has 1 image with `isMain: true` → Should display the image  
3. **Product ID 5 (mohammad)**: Has 1 image with `isMain: true` → Should display the image
4. **Product ID 1 (string)**: Has 3 images, first one with `isMain: true` → Should display the main image
5. **Products ID 2,3,4**: Have empty images arrays → Should show placeholder

The fix should now make all product images display correctly in the products listing page.