# Tenant Status Update (Activate/Deactivate) Fix

## Problem
The tenant edit page had activate/deactivate functionality in the UI, but the backend API didn't support changing tenant status through the update endpoint.

## Root Cause
The `TenantUpdateRequest` DTO only supported updating `name` and `customDomain`. It didn't have a `status` field, so status changes were ignored by the backend.

## Solution
Added full support for updating tenant status through the existing PUT `/saas/tenants/{id}` endpoint.

## Changes Made

### 1. DTO Layer (`TenantUpdateRequest.java`)
**Added `status` field** to allow status updates:

```java
public record TenantUpdateRequest(
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        @Pattern(
                regexp = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$",
                message = "Custom domain must be a valid domain name format"
        )
        @Size(max = 255, message = "Custom domain must not exceed 255 characters")
        String customDomain,

        TenantStatus status  // ← NEW FIELD
) {
}
```

**Added import:**
```java
import com.clinic.modules.core.tenant.TenantStatus;
```

### 2. Service Layer (`TenantManagementService.java`)
**Updated `updateTenant` method** to handle status changes:

```java
@Transactional
public TenantResponse updateTenant(Long id, TenantUpdateRequest request) {
    log.info("Tenant update initiated - tenantId: {}, name: {}, customDomain: {}, status: {}",
            id, request.name(), request.customDomain(), request.status());

    // ... retrieve tenant ...

    String oldName = tenant.getName();
    String oldCustomDomain = tenant.getCustomDomain();
    TenantStatus oldStatus = tenant.getStatus();  // ← Track old status

    // Update fields if provided
    if (request.name() != null && !request.name().isBlank()) {
        tenant.setName(request.name());
    }

    if (request.customDomain() != null) {
        tenant.setCustomDomain(request.customDomain().isBlank() ? null : request.customDomain());
    }

    if (request.status() != null) {  // ← Handle status update
        tenant.setStatus(request.status());
    }

    tenant = tenantRepository.save(tenant);
    log.info("Tenant updated successfully - tenantId: {}, slug: {}, nameChanged: {}, customDomainChanged: {}, statusChanged: {}",
            tenant.getId(), tenant.getSlug(),
            !oldName.equals(tenant.getName()),
            !java.util.Objects.equals(oldCustomDomain, tenant.getCustomDomain()),
            !oldStatus.equals(tenant.getStatus()));  // ← Log status change

    return mapToTenantResponse(tenant);
}
```

### 3. Controller Layer (`TenantManagementController.java`)
**Updated logging and documentation**:

```java
/**
 * Update an existing tenant.
 * Note: Slug cannot be modified after creation.
 *
 * @param id Tenant ID
 * @param request Update request with optional name, custom domain, and status
 * @return Updated tenant details (200 OK)
 */
@PutMapping("/{id}")
@PreAuthorize("hasRole('SAAS_MANAGER')")
public ResponseEntity<TenantResponse> updateTenant(
        @PathVariable Long id,
        @Valid @RequestBody TenantUpdateRequest request) {
    log.info("API request received - PUT /saas/tenants/{} - name: {}, customDomain: {}, status: {}",
            id, request.name(), request.customDomain(), request.status());
    TenantResponse response = tenantManagementService.updateTenant(id, request);
    log.info("API response sent - PUT /saas/tenants/{} - status: 200", id);
    return ResponseEntity.ok(response);
}
```

## API Usage

### Update Tenant Name Only
```bash
PUT /saas/tenants/1
{
  "name": "New Clinic Name",
  "customDomain": null,
  "status": null
}
```

### Activate a Tenant
```bash
PUT /saas/tenants/1
{
  "name": "My Clinic",
  "customDomain": null,
  "status": "ACTIVE"
}
```

### Deactivate a Tenant
```bash
PUT /saas/tenants/1
{
  "name": "My Clinic",
  "customDomain": null,
  "status": "INACTIVE"
}
```

### Suspend a Tenant
```bash
PUT /saas/tenants/1
{
  "name": "My Clinic",
  "customDomain": null,
  "status": "SUSPENDED"
}
```

## Frontend Integration

The frontend edit page (`pages/tenants/[id]/edit.vue`) already has the UI and logic for status changes:

```javascript
const handleStatusChange = async () => {
  if (!tenant.value) return

  statusChanging.value = true

  try {
    const newStatus = tenant.value.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'

    const updated = await api.updateTenant(tenantId.value, {
      name: tenant.value.name,
      customDomain: tenant.value.customDomain || undefined,
      status: newStatus  // ← This now works!
    } as any)

    toast.add({
      title: 'Status updated',
      description: `Tenant is now ${newStatus}`,
      color: 'green'
    })

    tenant.value = updated
    showStatusModal.value = false
  } catch (err: any) {
    // ... error handling
  } finally {
    statusChanging.value = false
  }
}
```

## How It Works

1. **User clicks Activate/Deactivate button** on tenant edit page
2. **Confirmation modal appears** asking to confirm the status change
3. **User confirms** → Frontend calls `api.updateTenant()` with the new status
4. **Backend receives request** → Updates tenant status in database
5. **Response returned** → Frontend shows success toast and updates UI
6. **Tenant status changed** → Reflected immediately in the UI

## Available Status Values
- `ACTIVE` - Tenant is active and users can access the system
- `INACTIVE` - Tenant is inactive and users cannot access
- `SUSPENDED` - Tenant is temporarily suspended

## Testing Checklist

- [x] Backend compiled successfully
- [x] API server restarted
- [ ] Test activating an inactive tenant
- [ ] Test deactivating an active tenant
- [ ] Test status change appears in tenant list
- [ ] Test status change appears in tenant detail page
- [ ] Test status badge updates correctly
- [ ] Verify logging shows status changes

## Status
✅ **Backend implemented and compiled**
✅ **API server restarted and healthy**
⏳ **Ready for testing**

## Next Steps
1. Navigate to a tenant edit page (e.g., `/tenants/1/edit`)
2. Scroll to "Status Management" section
3. Click "Activate" or "Deactivate" button
4. Confirm in the modal
5. Verify status changes successfully
6. Check tenant list to see updated status badge
