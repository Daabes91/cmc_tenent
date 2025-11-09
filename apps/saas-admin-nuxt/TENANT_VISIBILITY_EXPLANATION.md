# Tenant Visibility Explanation

## Issue
Only 1 tenant (the default one) is showing in the list, even though 2 tenants exist in the database.

## Root Cause
The second tenant (id: 1265, slug: "daabes") was **soft-deleted** and has a `deleted_at` timestamp. The API correctly filters out deleted tenants by default.

## Database State
```sql
 id  |  slug   |      name      | status |     deleted_at      |      created_at
-----+---------+----------------+--------+---------------------+---------------------
1265 | daabes  | new clinic     | ACTIVE | 2025-11-08 16:53:53 | 2025-11-08 16:43:23
   1 | default | Default Clinic | ACTIVE | NULL                | 2025-11-08 13:02:32
```

The tenant "daabes" was:
- Created at: 16:43:23
- Deleted at: 16:53:53 (10 minutes later)

## How Soft Delete Works

### Default Behavior (includeDeleted=false)
```bash
GET /saas/tenants
# Returns only non-deleted tenants (deleted_at IS NULL)
```

### Include Deleted Tenants
```bash
GET /saas/tenants?includeDeleted=true
# Returns ALL tenants including soft-deleted ones
```

## Solution

### Option 1: Create a New Tenant (Recommended)
Simply create a new tenant through the UI at `/tenants/new`. This will create a fresh tenant that is not deleted.

### Option 2: View Deleted Tenants
To see deleted tenants in the API response, add the query parameter:
```bash
curl 'http://localhost:8080/saas/tenants?includeDeleted=true' \
  -H 'authorization: Bearer YOUR_TOKEN'
```

### Option 3: Add UI Toggle (Future Enhancement)
Add a checkbox in the UI to toggle between:
- "Show active tenants only" (default)
- "Show all tenants including deleted"

## Why Soft Delete?

Soft delete is used instead of hard delete to:
1. **Preserve data** - Tenant data is archived, not destroyed
2. **Audit trail** - You can see when tenants were deleted
3. **Recovery** - Deleted tenants could potentially be restored
4. **Compliance** - Some regulations require data retention

## Testing

### Check all tenants in database:
```bash
docker exec clinic-postgres psql -U clinic -d clinic_multi_tenant \
  -c "SELECT id, slug, name, status, deleted_at FROM tenants ORDER BY created_at DESC;"
```

### Test API with includeDeleted:
```bash
# Only active tenants
curl 'http://localhost:8080/saas/tenants' -H 'authorization: Bearer TOKEN'

# All tenants including deleted
curl 'http://localhost:8080/saas/tenants?includeDeleted=true' -H 'authorization: Bearer TOKEN'
```

## Summary
The system is working correctly. The second tenant is not visible because it was soft-deleted. Create a new tenant to see multiple tenants in the list.
