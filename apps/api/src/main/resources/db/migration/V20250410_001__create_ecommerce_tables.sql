-- Add e-commerce feature flag to tenants table
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS ecommerce_enabled BOOLEAN NOT NULL DEFAULT FALSE;

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    sku VARCHAR(100),
    description TEXT,
    short_description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    product_type VARCHAR(20) NOT NULL DEFAULT 'SIMPLE',
    price DECIMAL(10,2),
    compare_at_price DECIMAL(10,2),
    currency VARCHAR(3) DEFAULT 'USD',
    has_variants BOOLEAN DEFAULT FALSE,
    is_taxable BOOLEAN DEFAULT TRUE,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraint to tenants table
    CONSTRAINT fk_products_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    
    -- Unique constraint: slug must be unique within a tenant
    CONSTRAINT uk_products_tenant_slug
        UNIQUE (tenant_id, slug)
);

-- Indexes for products table
CREATE INDEX IF NOT EXISTS idx_products_tenant ON products(tenant_id);
CREATE INDEX IF NOT EXISTS idx_products_tenant_status ON products(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_products_tenant_visible ON products(tenant_id, is_visible);

-- Create product variants table
CREATE TABLE IF NOT EXISTS product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    compare_at_price DECIMAL(10,2),
    currency VARCHAR(3) DEFAULT 'USD',
    stock_quantity INTEGER DEFAULT 0,
    is_in_stock BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraints
    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_variants_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    
    -- Unique constraint: SKU must be unique within a tenant
    CONSTRAINT uk_product_variants_tenant_sku
        UNIQUE (tenant_id, sku)
);

-- Indexes for product variants table
CREATE INDEX IF NOT EXISTS idx_product_variants_product ON product_variants(product_id);
CREATE INDEX IF NOT EXISTS idx_product_variants_tenant ON product_variants(tenant_id);

-- Create product images table
CREATE TABLE IF NOT EXISTS product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    sort_order INTEGER DEFAULT 0,
    is_main BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraints
    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_images_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

-- Indexes for product images table
CREATE INDEX IF NOT EXISTS idx_product_images_product ON product_images(product_id);
CREATE INDEX IF NOT EXISTS idx_product_images_tenant ON product_images(tenant_id);
CREATE INDEX IF NOT EXISTS idx_product_images_sort ON product_images(product_id, sort_order);

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    parent_id BIGINT,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraints
    CONSTRAINT fk_categories_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE CASCADE,
    
    -- Unique constraint: slug must be unique within a tenant
    CONSTRAINT uk_categories_tenant_slug
        UNIQUE (tenant_id, slug)
);

-- Indexes for categories table
CREATE INDEX IF NOT EXISTS idx_categories_tenant ON categories(tenant_id);
CREATE INDEX IF NOT EXISTS idx_categories_parent ON categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_tenant_active ON categories(tenant_id, is_active);

-- Create product categories junction table
CREATE TABLE IF NOT EXISTS product_categories (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraints
    CONSTRAINT fk_product_categories_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_categories_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_categories_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    
    -- Unique constraint: product-category combination must be unique
    CONSTRAINT uk_product_categories_product_category
        UNIQUE (product_id, category_id)
);

-- Indexes for product categories table
CREATE INDEX IF NOT EXISTS idx_product_categories_product ON product_categories(product_id);
CREATE INDEX IF NOT EXISTS idx_product_categories_category ON product_categories(category_id);
CREATE INDEX IF NOT EXISTS idx_product_categories_tenant ON product_categories(tenant_id);

-- Create carousels table
CREATE TABLE IF NOT EXISTS carousels (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    placement VARCHAR(100) NOT NULL,
    platform VARCHAR(20) NOT NULL DEFAULT 'BOTH',
    is_active BOOLEAN DEFAULT TRUE,
    max_items INTEGER DEFAULT 10,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraint
    CONSTRAINT fk_carousels_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    
    -- Unique constraint: slug must be unique within a tenant
    CONSTRAINT uk_carousels_tenant_slug
        UNIQUE (tenant_id, slug)
);

-- Indexes for carousels table
CREATE INDEX IF NOT EXISTS idx_carousels_tenant ON carousels(tenant_id);
CREATE INDEX IF NOT EXISTS idx_carousels_tenant_active ON carousels(tenant_id, is_active);
CREATE INDEX IF NOT EXISTS idx_carousels_placement_platform ON carousels(placement, platform);

-- Create carousel items table
CREATE TABLE IF NOT EXISTS carousel_items (
    id BIGSERIAL PRIMARY KEY,
    carousel_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    subtitle VARCHAR(255),
    image_url VARCHAR(500),
    link_url VARCHAR(500),
    cta_type VARCHAR(20) DEFAULT 'NONE',
    cta_text VARCHAR(100),
    product_id BIGINT,
    category_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraints
    CONSTRAINT fk_carousel_items_carousel
        FOREIGN KEY (carousel_id) REFERENCES carousels(id) ON DELETE CASCADE,
    CONSTRAINT fk_carousel_items_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_carousel_items_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_carousel_items_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Indexes for carousel items table
CREATE INDEX IF NOT EXISTS idx_carousel_items_carousel ON carousel_items(carousel_id);
CREATE INDEX IF NOT EXISTS idx_carousel_items_tenant ON carousel_items(tenant_id);
CREATE INDEX IF NOT EXISTS idx_carousel_items_sort ON carousel_items(carousel_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_carousel_items_product ON carousel_items(product_id);
CREATE INDEX IF NOT EXISTS idx_carousel_items_category ON carousel_items(category_id);

-- Create carts table
CREATE TABLE IF NOT EXISTS carts (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255),
    subtotal DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    
    -- Foreign key constraint
    CONSTRAINT fk_carts_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    
    -- Unique constraint: session must be unique within a tenant
    CONSTRAINT uk_carts_tenant_session
        UNIQUE (tenant_id, session_id)
);

-- Indexes for carts table
CREATE INDEX IF NOT EXISTS idx_carts_tenant ON carts(tenant_id);
CREATE INDEX IF NOT EXISTS idx_carts_session ON carts(session_id);
CREATE INDEX IF NOT EXISTS idx_carts_expires ON carts(expires_at);

-- Create cart items table
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraints
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    
    -- Unique constraint: cart-product-variant combination must be unique
    CONSTRAINT uk_cart_items_cart_product_variant
        UNIQUE (cart_id, product_id, variant_id)
);

-- Indexes for cart items table
CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_tenant ON cart_items(tenant_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product ON cart_items(product_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_variant ON cart_items(variant_id);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50),
    billing_address_line1 VARCHAR(255) NOT NULL,
    billing_address_line2 VARCHAR(255),
    billing_address_city VARCHAR(100) NOT NULL,
    billing_address_state VARCHAR(100),
    billing_address_postal_code VARCHAR(20),
    billing_address_country VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_PAYMENT',
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    shipping_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraint
    CONSTRAINT fk_orders_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    
    -- Unique constraint: order number must be unique within a tenant
    CONSTRAINT uk_orders_tenant_order_number
        UNIQUE (tenant_id, order_number)
);

-- Indexes for orders table
CREATE INDEX IF NOT EXISTS idx_orders_tenant ON orders(tenant_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_customer_email ON orders(customer_email);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);

-- Create order items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT,
    product_name VARCHAR(255) NOT NULL,
    variant_name VARCHAR(255),
    sku VARCHAR(100),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraints
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_order_items_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants(id)
);

-- Indexes for order items table
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_tenant ON order_items(tenant_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product ON order_items(product_id);
CREATE INDEX IF NOT EXISTS idx_order_items_variant ON order_items(variant_id);

-- Create ecommerce payments table (separate from core/treatment payments)
CREATE TABLE IF NOT EXISTS ecommerce_payments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'PAYPAL',
    provider_order_id VARCHAR(255),
    provider_payment_id VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    raw_response TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraints
    CONSTRAINT fk_payments_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes for ecommerce payments table
CREATE INDEX IF NOT EXISTS idx_ecommerce_payments_tenant ON ecommerce_payments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_ecommerce_payments_order ON ecommerce_payments(order_id);
CREATE INDEX IF NOT EXISTS idx_ecommerce_payments_provider_order ON ecommerce_payments(provider_order_id);
CREATE INDEX IF NOT EXISTS idx_ecommerce_payments_provider_payment ON ecommerce_payments(provider_payment_id);
CREATE INDEX IF NOT EXISTS idx_ecommerce_payments_status ON ecommerce_payments(status);

-- Add table comments
COMMENT ON TABLE products IS 'E-commerce products with tenant isolation and support for variants';
COMMENT ON TABLE product_variants IS 'Product variants for size, color, and other variations';
COMMENT ON TABLE product_images IS 'Product images with sort ordering and main image designation';
COMMENT ON TABLE categories IS 'Hierarchical product categories with tenant isolation';
COMMENT ON TABLE product_categories IS 'Many-to-many relationship between products and categories';
COMMENT ON TABLE carousels IS 'Content carousels for marketing and product display';
COMMENT ON TABLE carousel_items IS 'Individual items within carousels with various content types';
COMMENT ON TABLE carts IS 'Shopping carts with session-based management';
COMMENT ON TABLE cart_items IS 'Items within shopping carts';
COMMENT ON TABLE orders IS 'Customer orders with billing information';
COMMENT ON TABLE order_items IS 'Items within orders with snapshot of product information';
COMMENT ON TABLE ecommerce_payments IS 'Payment transactions linked to ecommerce orders';
