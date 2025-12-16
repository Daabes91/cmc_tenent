package com.clinic.modules.ecommerce.controller.admin;

import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.core.tenant.TenantNotFoundException;
import com.clinic.modules.ecommerce.config.RequiresEcommerceFeature;
import com.clinic.modules.ecommerce.dto.AdminUpdateOrderRequest;
import com.clinic.modules.ecommerce.dto.OrderResponse;
import com.clinic.modules.ecommerce.model.OrderEntity;
import com.clinic.modules.ecommerce.model.OrderStatus;
import com.clinic.modules.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin endpoints for managing ecommerce orders (tenant scoped).
 */
@RestController
@RequestMapping("/admin/tenants/{tenantId}/orders")
@Tag(name = "Admin Orders", description = "Tenant-scoped order management for admins")
@SecurityRequirement(name = "bearerAuth")
@RequiresEcommerceFeature
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    private final OrderService orderService;
    private final TenantContextHolder tenantContextHolder;
    private final TenantService tenantService;

    public AdminOrderController(OrderService orderService,
                                TenantContextHolder tenantContextHolder,
                                TenantService tenantService) {
        this.orderService = orderService;
        this.tenantContextHolder = tenantContextHolder;
        this.tenantService = tenantService;
    }

    @GetMapping
    @Operation(summary = "List orders", description = "List tenant orders with optional status or search filter.")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderResponse>> listOrders(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String email) {

        validateTenantAccess(tenantId);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        try {
            Page<OrderEntity> orders;
            TenantEntity tenantEntity = tenantService.requireActiveTenantById(tenantId);

            String normalizedSearch = (search != null && search.trim().length() >= 2 && !"all".equalsIgnoreCase(search.trim()))
                    ? search.trim()
                    : null;
            String normalizedEmail = (email != null && !email.trim().isEmpty()) ? email.trim() : null;

            if (normalizedSearch != null) {
                orders = orderService.searchOrdersByCustomer(tenantEntity, normalizedSearch, pageable);
            } else if (normalizedEmail != null) {
                orders = orderService.getOrdersByCustomerEmail(tenantEntity, normalizedEmail, pageable);
            } else if (status != null) {
                orders = orderService.getOrdersByStatus(tenantEntity, status, pageable);
            } else {
                orders = orderService.getOrders(tenantEntity, pageable);
            }

            return ResponseEntity.ok(orders.map(OrderResponse::fromEntity));
        } catch (TenantNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to list orders for tenant {}: {}", tenantId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to list orders", e);
        }
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details", description = "Fetch a single order by id for the tenant.")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long tenantId,
            @PathVariable Long orderId) {

        validateTenantAccess(tenantId);

        try {
            TenantEntity tenantEntity = tenantService.requireActiveTenantById(tenantId);
            OrderEntity order = orderService.getOrderById(tenantEntity, orderId);
            return ResponseEntity.ok(OrderResponse.fromEntityWithItems(order));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to fetch order {} for tenant {}: {}", orderId, tenantId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch order", e);
        }
    }

    private void validateTenantAccess(Long tenantId) {
        try {
            Long authenticatedTenantId = tenantContextHolder.requireTenantId();
            if (authenticatedTenantId != null && !authenticatedTenantId.equals(tenantId)) {
                throw new IllegalArgumentException("Access denied to tenant: " + tenantId);
            }
        } catch (Exception ignored) {
            // If no tenant is bound to the context (e.g., during admin requests), skip strict validation.
        }
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "Update order", description = "Admin updates to order details and status.")
    @Transactional
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long tenantId,
            @PathVariable Long orderId,
            @RequestBody AdminUpdateOrderRequest request) {

        validateTenantAccess(tenantId);

        try {
            TenantEntity tenantEntity = tenantService.requireActiveTenantById(tenantId);
            OrderEntity updated = orderService.updateOrder(tenantEntity, orderId, request);
            return ResponseEntity.ok(OrderResponse.fromEntityWithItems(updated));
        } catch (TenantNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to update order {} for tenant {}: {}", orderId, tenantId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update order", e);
        }
    }
}
