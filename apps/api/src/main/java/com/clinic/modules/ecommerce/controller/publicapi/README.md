# Public Product Browsing API

This document describes the public API endpoints for product browsing functionality.

## Base URL

All endpoints are under `/public/products`

## Authentication

These are public endpoints that do not require authentication. However, they do require tenant resolution through either:
- `slug` parameter: The tenant's slug identifier
- `domain` parameter: The tenant's custom domain

## Endpoints

### 1. List Products

**GET** `/public/products`

Get all visible products with optional filtering and pagination.

**Parameters:**
- `slug` (optional): Tenant slug for resolution
- `domain` (optional): Tenant domain for resolution  
- `category` (optional): Filter by category ID
- `search` (optional): Search term for product name/description
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter
- `page` (optional, default: 0): Page number (0-based)
- `size` (optional, default: 20, max: 100): Page size
- `sort` (optional, default: createdAt,desc): Sort criteria

**Response:**
```json
{
  "products": [
    {
      "id": 1,
      "name": "Product Name",
      "slug": "product-slug",
      "description": "Product description",
      "shortDescription": "Short description",
      "status": "ACTIVE",
      "price": 99.99,
      "compareAtPrice": 129.99,
      "currency": "USD",
      "hasVariants": false,
      "isTaxable": true,
      "isVisible": true,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z",
      "variants": [],
      "images": [],
      "categories": []
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "total": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

### 2. Get Product by ID

**GET** `/public/products/{productId}`

Get a specific product by its ID.

**Parameters:**
- `productId` (path): The product ID
- `slug` (optional): Tenant slug for resolution
- `domain` (optional): Tenant domain for resolution

**Response:**
Same as individual product object from list endpoint.

### 3. Get Product by Slug

**GET** `/public/products/by-slug/{productSlug}`

Get a specific product by its slug.

**Parameters:**
- `productSlug` (path): The product slug
- `slug` (optional): Tenant slug for resolution
- `domain` (optional): Tenant domain for resolution

**Response:**
Same as individual product object from list endpoint.

### 4. Search Products

**GET** `/public/products/search`

Search products by term.

**Parameters:**
- `q` (required): Search term
- `slug` (optional): Tenant slug for resolution
- `domain` (optional): Tenant domain for resolution
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size
- `sort` (optional, default: createdAt,desc): Sort criteria

**Response:**
Same as list products endpoint.

### 5. Get Products by Category

**GET** `/public/products/category/{categoryId}`

Get products in a specific category.

**Parameters:**
- `categoryId` (path): The category ID
- `slug` (optional): Tenant slug for resolution
- `domain` (optional): Tenant domain for resolution
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size
- `sort` (optional, default: createdAt,desc): Sort criteria

**Response:**
Same as list products endpoint.

### 6. Get Recent Products

**GET** `/public/products/recent`

Get recently added products.

**Parameters:**
- `slug` (optional): Tenant slug for resolution
- `domain` (optional): Tenant domain for resolution
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size

**Response:**
Same as list products endpoint (always sorted by creation date descending).

## Error Responses

### 400 Bad Request
- Invalid tenant slug/domain
- Invalid pagination parameters
- Missing required parameters

### 404 Not Found
- Product not found
- Product not visible/active

### 500 Internal Server Error
- System error

## Data Model

### Product
- `id`: Unique product identifier
- `name`: Product name
- `slug`: URL-friendly product identifier
- `description`: Full product description
- `shortDescription`: Brief product description
- `status`: Product status (DRAFT, ACTIVE, ARCHIVED)
- `price`: Product price
- `compareAtPrice`: Original/compare price
- `currency`: Price currency code
- `hasVariants`: Whether product has variants
- `isTaxable`: Whether product is taxable
- `isVisible`: Whether product is visible to customers
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp
- `variants`: List of product variants
- `images`: List of product images
- `categories`: List of associated categories

### Product Variant
- `id`: Unique variant identifier
- `sku`: Stock keeping unit
- `name`: Variant name
- `price`: Variant price
- `compareAtPrice`: Original/compare price
- `currency`: Price currency code
- `stockQuantity`: Available stock
- `isInStock`: Stock availability flag
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

### Product Image
- `id`: Unique image identifier
- `imageUrl`: Image URL
- `altText`: Alternative text for accessibility
- `sortOrder`: Display order
- `isMain`: Whether this is the main product image
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

### Category
- `id`: Unique category identifier
- `name`: Category name
- `slug`: URL-friendly category identifier
- `description`: Category description
- `sortOrder`: Display order
- `isActive`: Whether category is active
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

## Usage Examples

### Get all products for a tenant
```
GET /public/products?slug=my-clinic
```

### Search for products
```
GET /public/products/search?q=dental&slug=my-clinic
```

### Get products in a category with price filter
```
GET /public/products?category=5&minPrice=50&maxPrice=200&slug=my-clinic
```

### Get product details
```
GET /public/products/123?slug=my-clinic
GET /public/products/by-slug/dental-cleaning?slug=my-clinic
```