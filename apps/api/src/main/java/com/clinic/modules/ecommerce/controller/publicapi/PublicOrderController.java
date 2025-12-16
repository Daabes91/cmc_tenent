package com.clinic.modules.ecommerce.controller.publicapi;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.ecommerce.dto.BuyNowRequest;
import com.clinic.modules.ecommerce.dto.CreateOrderRequest;
import com.clinic.modules.ecommerce.dto.OrderResponse;
import com.clinic.modules.ecommerce.exception.InvalidCartStateException;
import com.clinic.modules.ecommerce.exception.ProductNotFoundException;
import com.clinic.modules.ecommerce.model.OrderEntity;
import com.clinic.modules.ecommerce.service.EcommerceFeatureService;
import com.clinic.modules.ecommerce.service.OrderNotificationService;
import com.clinic.modules.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Public API controller for order management.
 * Handles order creation, retrieval, and customer order history.
 */
@RestController
@RequestMapping("/public/orders")
@Tag(name = "Public Orders", description = "Public order management API")
public class PublicOrderController {

    private static final Logger logger = LoggerFactory.getLogger(PublicOrderController.class);

    private final OrderService orderService;
    private final TenantService tenantService;
    private final EcommerceFeatureService ecommerceFeatureService;
    private final OrderNotificationService orderNotificationService;

    @Autowired
    public PublicOrderController(OrderService orderService,
                                TenantService tenantService,
                                EcommerceFeatureService ecommerceFeatureService,
                                OrderNotificationService orderNotificationService) {
        this.orderService = orderService;
        this.tenantService = tenantService;
        this.ecommerceFeatureService = ecommerceFeatureService;
        this.orderNotificationService = orderNotificationService;
    }

    /**
     * Resolve tenant from slug or domain parameter.
     */
    private TenantEntity resolveTenant(String tenantParam) {
        TenantEntity tenant = null;
        
        // Try to resolve by slug first
        if (tenantParam != null && !tenantParam.trim().isEmpty()) {
            tenant = tenantService.findActiveBySlug(tenantParam).orElse(null);
            
            // Try to resolve by domain if slug didn't work
            if (tenant == null) {
                tenant = tenantService.findActiveByDomain(tenantParam).orElse(null);
            }
        }
        
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant not found: " + tenantParam);
        }
        
        return tenant;
    }

    @Operation(summary = "Create order from cart", 
               description = "Create a new order from cart contents with customer information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or cart state"),
        @ApiResponse(responseCode = "404", description = "Tenant not found or cart empty"),
        @ApiResponse(responseCode = "503", description = "E-commerce feature not enabled")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @Parameter(description = "Tenant slug or domain", required = true)
            @RequestParam String tenant,
            @Parameter(description = "Order creation request", required = true)
            @Valid @RequestBody CreateOrderRequest request) {
        
        logger.debug("Creating order for tenant: {}", tenant);

        try {
            // Resolve tenant
            TenantEntity tenantEntity = resolveTenant(tenant);
            
            // Check e-commerce feature
            ecommerceFeatureService.validateEcommerceEnabled(tenantEntity.getId());

            // Create order
            OrderEntity order = orderService.createOrderFromCart(tenantEntity, request);

            // Clear cart after successful order creation
            orderService.clearCartAfterOrder(tenantEntity, request.getSessionId());

            // Notify the customer by email (non-blocking on failures)
            orderNotificationService.sendOrderConfirmationEmail(order);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("order", OrderResponse.fromEntityWithItems(order));

            logger.info("Successfully created order {} for tenant {}", 
                       order.getOrderNumber(), tenantEntity.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InvalidCartStateException e) {
            logger.warn("Invalid cart state for order creation: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "INVALID_CART_STATE");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (ProductNotFoundException e) {
            logger.warn("Product not found during order creation: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "PRODUCT_NOT_FOUND");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request while creating order: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "INVALID_REQUEST");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Error creating order for tenant {}: {}", tenant, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "ORDER_CREATION_FAILED");
            errorResponse.put("message", "Failed to create order. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Buy now - Create order directly from product", 
               description = "Create a new order directly from a single product without using cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or insufficient stock"),
        @ApiResponse(responseCode = "404", description = "Tenant or product not found"),
        @ApiResponse(responseCode = "503", description = "E-commerce feature not enabled")
    })
    @PostMapping("/buy-now")
    public ResponseEntity<Map<String, Object>> buyNow(
            @Parameter(description = "Tenant slug or domain", required = true)
            @RequestParam String tenant,
            @Parameter(description = "Buy now request", required = true)
            @Valid @RequestBody BuyNowRequest request) {
        
        logger.debug("Processing buy now for product {} and tenant: {}", request.getProductId(), tenant);

        try {
            // Resolve tenant
            TenantEntity tenantEntity = resolveTenant(tenant);
            
            // Check e-commerce feature
            ecommerceFeatureService.validateEcommerceEnabled(tenantEntity.getId());

            // Create order directly
            OrderEntity order = orderService.createDirectOrder(tenantEntity, request);

            // Notify the customer by email (non-blocking on failures)
            orderNotificationService.sendOrderConfirmationEmail(order);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("order", OrderResponse.fromEntityWithItems(order));

            logger.info("Successfully created buy now order {} for tenant {}", 
                       order.getOrderNumber(), tenantEntity.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InvalidCartStateException e) {
            logger.warn("Invalid product state for buy now: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "INVALID_PRODUCT_STATE");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (ProductNotFoundException e) {
            logger.warn("Product not found during buy now: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "PRODUCT_NOT_FOUND");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request while processing buy now: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "INVALID_REQUEST");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Error processing buy now for tenant {}: {}", tenant, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "BUY_NOW_FAILED");
            errorResponse.put("message", "Failed to process buy now. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Get order by number", 
               description = "Retrieve order details by order number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "503", description = "E-commerce feature not enabled")
    })
    @GetMapping("/{orderNumber}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getOrder(
            @Parameter(description = "Tenant slug or domain", required = true)
            @RequestParam String tenant,
            @Parameter(description = "Order number", required = true)
            @PathVariable String orderNumber) {
        
        logger.debug("Getting order {} for tenant: {}", orderNumber, tenant);

        try {
            // Resolve tenant
            TenantEntity tenantEntity = resolveTenant(tenant);
            
            // Check e-commerce feature
            ecommerceFeatureService.validateEcommerceEnabled(tenantEntity.getId());

            // Get order
            Optional<OrderEntity> orderOpt = orderService.getOrderByNumber(tenantEntity, orderNumber);
            
            if (orderOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "ORDER_NOT_FOUND");
                errorResponse.put("message", "Order not found: " + orderNumber);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("order", OrderResponse.fromEntityWithItems(orderOpt.get()));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request while retrieving order {}: {}", orderNumber, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "INVALID_REQUEST");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Error getting order {} for tenant {}: {}", orderNumber, tenant, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "ORDER_RETRIEVAL_FAILED");
            errorResponse.put("message", "Failed to retrieve order. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Get customer orders", 
               description = "Retrieve order history for a customer by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "503", description = "E-commerce feature not enabled")
    })
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getCustomerOrders(
            @Parameter(description = "Tenant slug or domain", required = true)
            @RequestParam String tenant,
            @Parameter(description = "Customer email", required = true)
            @RequestParam String email,
            @Parameter(description = "Page number (0-based)", required = false)
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false)
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", required = false)
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", required = false)
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.debug("Getting orders for customer {} and tenant: {}", email, tenant);

        try {
            // Resolve tenant
            TenantEntity tenantEntity = resolveTenant(tenant);
            
            // Check e-commerce feature
            ecommerceFeatureService.validateEcommerceEnabled(tenantEntity.getId());

            // Validate email
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "INVALID_EMAIL");
                errorResponse.put("message", "Customer email is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Create pageable
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by(direction, sortBy));

            // Get orders
            Page<OrderEntity> ordersPage = orderService.getOrdersByCustomerEmail(tenantEntity, email, pageable);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", ordersPage.getContent().stream()
                .map(OrderResponse::fromEntity)
                .toList());
            
            // Pagination metadata
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page", ordersPage.getNumber());
            pagination.put("pageSize", ordersPage.getSize());
            pagination.put("totalElements", ordersPage.getTotalElements());
            pagination.put("totalPages", ordersPage.getTotalPages());
            pagination.put("hasMore", ordersPage.hasNext());
            response.put("pagination", pagination);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request while listing orders: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "INVALID_REQUEST");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Error getting orders for customer {} and tenant {}: {}", 
                        email, tenant, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "ORDER_RETRIEVAL_FAILED");
            errorResponse.put("message", "Failed to retrieve orders. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Search orders", 
               description = "Search orders by customer name or email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "503", description = "E-commerce feature not enabled")
    })
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> searchOrders(
            @Parameter(description = "Tenant slug or domain", required = true)
            @RequestParam String tenant,
            @Parameter(description = "Search term (customer name or email)", required = true)
            @RequestParam String q,
            @Parameter(description = "Page number (0-based)", required = false)
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false)
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("Searching orders with term '{}' for tenant: {}", q, tenant);

        try {
            // Resolve tenant
            TenantEntity tenantEntity = resolveTenant(tenant);
            
            // Check e-commerce feature
            ecommerceFeatureService.validateEcommerceEnabled(tenantEntity.getId());

            // Validate search term
            if (q == null || q.trim().length() < 2) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "INVALID_SEARCH_TERM");
                errorResponse.put("message", "Search term must be at least 2 characters long");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Create pageable
            Pageable pageable = PageRequest.of(page, Math.min(size, 50), 
                Sort.by(Sort.Direction.DESC, "createdAt"));

            // Search orders
            Page<OrderEntity> ordersPage = orderService.searchOrdersByCustomer(tenantEntity, q.trim(), pageable);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", ordersPage.getContent().stream()
                .map(OrderResponse::fromEntity)
                .toList());
            
            // Pagination metadata
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page", ordersPage.getNumber());
            pagination.put("pageSize", ordersPage.getSize());
            pagination.put("totalElements", ordersPage.getTotalElements());
            pagination.put("totalPages", ordersPage.getTotalPages());
            pagination.put("hasMore", ordersPage.hasNext());
            response.put("pagination", pagination);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request while searching orders: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "INVALID_REQUEST");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Error searching orders for tenant {}: {}", tenant, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "ORDER_SEARCH_FAILED");
            errorResponse.put("message", "Failed to search orders. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
