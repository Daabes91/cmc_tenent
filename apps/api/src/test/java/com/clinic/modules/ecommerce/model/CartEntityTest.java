package com.clinic.modules.ecommerce.model;

import com.clinic.modules.core.tenant.TenantEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CartEntity to verify basic functionality.
 */
public class CartEntityTest {

    @Test
    public void testCartCreation() {
        TenantEntity tenant = new TenantEntity("test-tenant", "Test Tenant");
        
        CartEntity cart = new CartEntity(tenant, "session123");
        
        assertNotNull(cart);
        assertEquals("session123", cart.getSessionId());
        assertEquals(tenant, cart.getTenant());
        assertEquals(BigDecimal.ZERO, cart.getSubtotal());
        assertEquals(BigDecimal.ZERO, cart.getTaxAmount());
        assertEquals(BigDecimal.ZERO, cart.getTotalAmount());
        assertEquals("USD", cart.getCurrency());
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getTotalItemCount());
        assertNotNull(cart.getCreatedAt());
        assertNotNull(cart.getUpdatedAt());
        assertNotNull(cart.getExpiresAt());
    }

    @Test
    public void testCartTotalCalculation() {
        TenantEntity tenant = new TenantEntity("test-tenant", "Test Tenant");
        
        CartEntity cart = new CartEntity(tenant, "session123");
        
        // Create a mock product and item
        ProductEntity product = new ProductEntity(tenant, "Test Product", "test-product");
        product.setPrice(new BigDecimal("10.00"));
        
        CartItemEntity item = new CartItemEntity(cart, product, 2, new BigDecimal("10.00"));
        cart.addItem(item);
        
        assertEquals(new BigDecimal("20.00"), cart.getSubtotal());
        assertEquals(0, new BigDecimal("1.60").compareTo(cart.getTaxAmount())); // 8% tax
        assertEquals(0, new BigDecimal("21.60").compareTo(cart.getTotalAmount()));
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getTotalItemCount());
        assertFalse(cart.isEmpty());
    }
}