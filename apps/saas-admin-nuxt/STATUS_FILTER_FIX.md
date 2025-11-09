# Tenant Status Filter Fix

## Problem
The tenant list page's status filter was not working. When selecting "All Statuses", the frontend sent no `status` parameter, but the backend didn't support filtering by status at all.

## Root Cause
The backend API endpoint `/saas/tenants` didn't accept or process a `status` parameter for filtering tenants by their status (ACTIVE, INACTIVE, SUSPENDED).

## Solution
Added full support for status filtering across all backend layers.

## Changes Made

### 1. Repository Layer (`TenantRepository.java`)
**Added new query method** to support filtering by status:

```java
@Query("SELECT t FROM TenantEntity t WHERE " +
       "(:includeDeleted = true OR t.deletedAt IS NULL) AND " +
       "(:status IS NULL OR t.status = :status)")
Page<TenantEntity> findAllWithFilters(
    @Param("includeDeleted") boolean includeDeleted,
    @Param("status") TenantStatus status,
    Pageable pageable
);
```

**Key Features:**
- Supports optional status filtering (null = all statuses)
- Combines with existing `includeDeleted` filter
- Uses pagination for efficient querying

### 2. Service Layer (`TenantManagementService.java`)
**Updated `listTenants` method** to accept and pass status parameter:

```java
@Transactional(readOnly = true)
public TenantListResponse listTenants(
    Pageable pageable,
    boolean includeDeleted,
    TenantStatus status
) {
    log.debug("Tenant list requested - page: {}, size: {}, includeDeleted: {}, status: {}",
            pageable.getPageNumber(), pageable.getPageSize(), includeDeleted, status);

    Page<TenantEntity> page = tenantRepository.findAllWithFilters(
        includeDeleted,
        status,
        pageable
    );

    // ... rest of method
}
```

### 3. Controller Layer (`TenantManagementController.java`)
**Updated `listTenants` endpoint** to accept status parameter:

```java
@GetMapping
@PreAuthorize("hasRole('SAAS_MANAGER')")
public ResponseEntity<TenantListResponse> listTenants(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "false") boolean includeDeleted,
        @RequestParam(required = false) TenantStatus status) {
    // ...
    TenantListResponse response = tenantManagementService.listTenants(
        pageable,
        includeDeleted,
        status
    );
    return ResponseEntity.ok(response);
}
```

**Added import:**
```java
import com.clinic.modules.core.tenant.TenantStatus;
```

## API Usage

### Get All Tenants (Any Status)
```bash
GET /saas/tenants?page=0&size=20
# Returns: ACTIVE, INACTIVE, and SUSPENDED tenants
```

### Get Only Active Tenants
```bash
GET /saas/tenants?page=0&size=20&status=ACTIVE
# Returns: Only ACTIVE tenants
```

### Get Only Inactive Tenants
```bash
GET /saas/tenants?page=0&size=20&status=INACTIVE
# Returns: Only INACTIVE tenants
```

### Get Only Suspended Tenants
```bash
GET /saas/tenants?page=0&size=20&status=SUSPENDED
# Returns: Only SUSPENDED tenants
```

## Frontend Integration

The frontend (`pages/tenants/index.vue`) already correctly handles the status filter:

```javascript
const statusOptions = [
  { label: 'All Statuses', value: 'all' },
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Inactive', value: 'INACTIVE' }
]

// In loadTenants:
if (filters.status !== 'all') params.status = filters.status
```

When `status === 'all'`, no status parameter is sent, and the backend returns all tenants.
When a specific status is selected, that status value is sent to the backend.

## Testing Checklist

- [x] Compile backend successfully
- [x] Backend API starts without errors
- [ ] Test "All Statuses" - should return all tenants
- [ ] Test "Active" filter - should return only ACTIVE tenants
- [ ] Test "Inactive" filter - should return only INACTIVE tenants
- [ ] Test pagination works with status filters
- [ ] Test search + status filter combination

## Status
✅ **Backend implemented and compiled**
✅ **API server restarted**
⏳ **Ready for frontend testing**

## Next Steps
1. Refresh the tenant list page in the browser
2. Test the status filter dropdown:
   - Select "All Statuses" → Should show all tenants
   - Select "Active" → Should show only active tenants
   - Select "Inactive" → Should show only inactive tenants
3. Verify pagination works with each filter option
