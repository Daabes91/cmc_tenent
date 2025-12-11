package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.ecommerce.dto.CreateOrderRequest;
import com.clinic.modules.ecommerce.exception.InvalidCartStateException;
import com.clinic.modules.ecommerce.model.*;
import com.clinic.modules.ecommerce.repository.OrderItemRepository;
import com.clinic.modules.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private TenantEntity tenant;
    private CreateOrderRequest createOrderRequest;
    private CartEntity cart;
    private ProductEntity product;
    private CartItemEntity cartItem;

    @BeforeEach
    void setUp() {
        tenant = new TenantEntity("test-clinic", "Test Clinic");

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setSessionId("test-session");
        createOrderRequest.setCustomerName("John Doe");
        createOrderRequest.setCustomerEmail("john@example.com");
        createOrderRequest.setCustomerPhone("+1234567890");
        createOrderRequest.setBillingAddressLine1("123 Main St");
        createOrderRequest.setBillingAddressCity("Test City");
        createOrderRequest.setBillingAddressCountry("Test Country");

        product = new ProductEntity(tenant, "Test Product", "test-product");
        product.setSku("TEST-001");
        product.setPrice(new BigDecimal("29.99"));
        product.setCurrency("USD");
        product.setStatus(ProductStatus.ACTIVE);

        cart = new CartEntity(tenant, "test-session");
        cart.setId(1L);
        cart.setSubtotal(new BigDecimal("29.99"));
        cart.setTaxAmount(new BigDecimal("2.40"));
        cart.setTotalAmount(new BigDecimal("32.39"));

        cartItem = new CartItemEntity(cart, product, 1, new BigDecimal("29.99"));
        cartItem.setId(1L);
        cart.addItem(cartItem);
    }

    @Test
    void createOrderFromCart_Success() {
        // Arrange
        when(cartService.getCart(tenant, "test-session")).thenReturn(Optional.of(cart));
        when(cartService.validateCartAvailability(tenant, "test-session")).thenReturn(List.of());
        when(orderRepository.existsByTenantAndOrderNumber(eq(tenant.getId()), anyString())).thenReturn(false);
        
        OrderEntity savedOrder = new OrderEntity(tenant, "0001-20241210-1234");
        savedOrder.setId(1L);
        savedOrder.setCustomerName("John Doe");
        savedOrder.setCustomerEmail("john@example.com");
        savedOrder.setStatus(OrderStatus.PENDING_PAYMENT);
        savedOrder.setSubtotal(new BigDecimal("29.99"));
        savedOrder.setTaxAmount(new BigDecimal("2.40"));
        savedOrder.setTotalAmount(new BigDecimal("32.39"));
        
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        when(orderItemRepository.save(any(OrderItemEntity.class))).thenReturn(new OrderItemEntity());

        // Act
        OrderEntity result = orderService.createOrderFromCart(tenant, createOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals("john@example.com", result.getCustomerEmail());
        assertEquals(OrderStatus.PENDING_PAYMENT, result.getStatus());
        assertEquals(new BigDecimal("29.99"), result.getSubtotal());
        assertEquals(new BigDecimal("32.39"), result.getTotalAmount());

        verify(cartService).getCart(tenant, "test-session");
        verify(cartService).validateCartAvailability(tenant, "test-session");
        verify(orderRepository, atLeastOnce()).save(any(OrderEntity.class));
        verify(orderItemRepository).save(any(OrderItemEntity.class));
    }

    @Test
    void createOrderFromCart_CartNotFound() {
        // Arrange
        when(cartService.getCart(tenant, "test-session")).thenReturn(Optional.empty());

        // Act & Assert
        InvalidCartStateException exception = assertThrows(
            InvalidCartStateException.class,
            () -> orderService.createOrderFromCart(tenant, createOrderRequest)
        );

        assertEquals("Cart not found for session: test-session", exception.getMessage());
        verify(cartService).getCart(tenant, "test-session");
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void createOrderFromCart_EmptyCart() {
        // Arrange
        CartEntity emptyCart = new CartEntity(tenant, "test-session");
        when(cartService.getCart(tenant, "test-session")).thenReturn(Optional.of(emptyCart));

        // Act & Assert
        InvalidCartStateException exception = assertThrows(
            InvalidCartStateException.class,
            () -> orderService.createOrderFromCart(tenant, createOrderRequest)
        );

        assertEquals("Cannot create order from empty cart", exception.getMessage());
        verify(cartService).getCart(tenant, "test-session");
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void createOrderFromCart_UnavailableItems() {
        // Arrange
        when(cartService.getCart(tenant, "test-session")).thenReturn(Optional.of(cart));
        when(cartService.validateCartAvailability(tenant, "test-session")).thenReturn(List.of(cartItem));

        // Act & Assert
        InvalidCartStateException exception = assertThrows(
            InvalidCartStateException.class,
            () -> orderService.createOrderFromCart(tenant, createOrderRequest)
        );

        assertTrue(exception.getMessage().contains("Some items are no longer available"));
        verify(cartService).getCart(tenant, "test-session");
        verify(cartService).validateCartAvailability(tenant, "test-session");
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void getOrder_Success() {
        // Arrange
        OrderEntity order = new OrderEntity(tenant, "0001-20241210-1234");
        order.setId(1L);
        when(orderRepository.findByIdAndTenant(1L, tenant.getId())).thenReturn(Optional.of(order));

        // Act
        Optional<OrderEntity> result = orderService.getOrder(tenant, 1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository).findByIdAndTenant(1L, tenant.getId());
    }

    @Test
    void getOrder_NotFound() {
        // Arrange
        when(orderRepository.findByIdAndTenant(1L, tenant.getId())).thenReturn(Optional.empty());

        // Act
        Optional<OrderEntity> result = orderService.getOrder(tenant, 1L);

        // Assert
        assertFalse(result.isPresent());
        verify(orderRepository).findByIdAndTenant(1L, tenant.getId());
    }

    @Test
    void updateOrderStatus_Success() {
        // Arrange
        OrderEntity order = new OrderEntity(tenant, "0001-20241210-1234");
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        
        when(orderRepository.findByIdAndTenant(1L, tenant.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

        // Act
        OrderEntity result = orderService.updateOrderStatus(tenant, 1L, OrderStatus.PAID);

        // Assert
        assertEquals(OrderStatus.PAID, result.getStatus());
        verify(orderRepository).findByIdAndTenant(1L, tenant.getId());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_Success() {
        // Arrange
        OrderEntity order = new OrderEntity(tenant, "0001-20241210-1234");
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        
        when(orderRepository.findByIdAndTenant(1L, tenant.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

        // Act
        OrderEntity result = orderService.cancelOrder(tenant, 1L, "Customer request");

        // Assert
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertTrue(result.getNotes().contains("Cancelled: Customer request"));
        verify(orderRepository).findByIdAndTenant(1L, tenant.getId());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_CannotBeCancelled() {
        // Arrange
        OrderEntity order = new OrderEntity(tenant, "0001-20241210-1234");
        order.setId(1L);
        order.setStatus(OrderStatus.SHIPPED); // Cannot be cancelled
        
        when(orderRepository.findByIdAndTenant(1L, tenant.getId())).thenReturn(Optional.of(order));

        // Act & Assert
        InvalidCartStateException exception = assertThrows(
            InvalidCartStateException.class,
            () -> orderService.cancelOrder(tenant, 1L, "Customer request")
        );

        assertTrue(exception.getMessage().contains("Order cannot be cancelled in current status"));
        verify(orderRepository).findByIdAndTenant(1L, tenant.getId());
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }
}