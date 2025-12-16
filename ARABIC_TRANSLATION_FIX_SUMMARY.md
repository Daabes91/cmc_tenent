# Arabic Translation Fix for Product Cards

## Issue
The "Add to cart" button and other product-related text were not being translated to Arabic when the interface language was set to Arabic. The text remained in English even when all other parts of the interface were properly translated.

## Root Cause
The ProductCard and ProductGrid components were using hardcoded English strings instead of utilizing the translation system (next-intl) that was already implemented in the project.

## Files Modified

### 1. ProductCard Component (`apps/web-next/components/ProductCard.tsx`)
**Changes:**
- Added `useTranslations` import from 'next-intl'
- Added `const t = useTranslations('ecommerce')` to get translations
- Replaced hardcoded strings with translation keys:
  - `"Add to cart"` → `{t('addToCart')}`
  - `"Adding..."` → `{t('adding')}`
  - `"Added to cart"` → `{t('addedToCart')}`
  - `"Contact for price"` → `{t('contactForPrice')}`
  - `"No image"` → `{t('noImage')}`
  - `"SALE"` → `{t('sale')}`
- Fixed TypeScript error by adding null check for `product.price`

### 2. ProductGrid Component (`apps/web-next/components/ProductGrid.tsx`)
**Changes:**
- Added `useTranslations` import from 'next-intl'
- Added `const t = useTranslations('ecommerce')` to get translations
- Replaced hardcoded strings with translation keys:
  - `"No products found"` → `{t('noProductsFound')}`
  - `"Try adjusting your search or filter criteria."` → `{t('adjustSearchCriteria')}`

### 3. Product Detail Page (`apps/web-next/app/[locale]/(site)/products/[slug]/page.tsx`)
**Changes:**
- Added `useTranslations` import to existing next-intl import
- Added `const t = useTranslations('ecommerce')` to get translations
- Replaced hardcoded button text with translation keys:
  - `'Adding…'` → `t('adding')`
  - `'Added to cart'` → `t('addedToCart')`
  - `'Add to cart'` → `t('addToCart')`

### 4. Translation Files

#### English (`apps/web-next/messages/en.json`)
Added new keys to the `ecommerce` section:
```json
"ecommerce": {
  "featuredProducts": "Featured products",
  "viewAllProducts": "View all products",
  "addToCart": "Add to cart",
  "adding": "Adding…",
  "addedToCart": "Added to cart",
  "contactForPrice": "Contact for price",
  "noImage": "No image",
  "sale": "SALE",
  "noProductsFound": "No products found",
  "adjustSearchCriteria": "Try adjusting your search or filter criteria."
}
```

#### Arabic (`apps/web-next/messages/ar.json`)
Added corresponding Arabic translations:
```json
"ecommerce": {
  "featuredProducts": "المنتجات المميزة",
  "viewAllProducts": "عرض جميع المنتجات",
  "addToCart": "أضف إلى السلة",
  "adding": "جاري الإضافة…",
  "addedToCart": "تمت الإضافة إلى السلة",
  "contactForPrice": "تواصل لمعرفة السعر",
  "noImage": "لا توجد صورة",
  "sale": "تخفيض",
  "noProductsFound": "لم يتم العثور على منتجات",
  "adjustSearchCriteria": "جرب تعديل معايير البحث أو التصفية."
}
```

## Files Already Properly Configured
- `apps/web-next/app/[locale]/(site)/products/ProductsPageClient.tsx` - This file was already using the translation system correctly with the `productsPage.actions` namespace.

## Result
Now when the interface is set to Arabic:
- "Add to cart" button displays as "أضف إلى السلة"
- "Adding..." displays as "جاري الإضافة…"
- "Added to cart" displays as "تمت الإضافة إلى السلة"
- "Contact for price" displays as "تواصل لمعرفة السعر"
- "No image" displays as "لا توجد صورة"
- "SALE" badge displays as "تخفيض"
- Empty state messages are properly translated

## Testing
To test the fix:
1. Navigate to the products page
2. Switch the language to Arabic using the language selector
3. Verify that all product card text is now displayed in Arabic
4. Test the "Add to cart" functionality to ensure the button states change with proper Arabic text

## Technical Notes
- The fix uses the existing next-intl translation system that was already implemented in the project
- All translations follow the existing pattern used throughout the application
- TypeScript errors were resolved by adding proper null checks
- The solution is consistent with how other components in the project handle translations