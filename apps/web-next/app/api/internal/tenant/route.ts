import { NextRequest, NextResponse } from 'next/server'
import { TENANT_HEADER } from '@/lib/tenant'
import { dbQuery } from '@/lib/db'
import type { TenantInfo, ApiErrorResponse } from '@/lib/theme-types'

/**
 * GET /api/internal/tenant
 * 
 * Internal API endpoint that returns the current tenant's information based on the x-tenant header.
 * This endpoint is used by the theme settings page to determine the current tenant and their theme selection.
 * 
 * The x-tenant header is set by the middleware during tenant resolution.
 * 
 * @param {NextRequest} request - The incoming request with x-tenant header
 * @returns {Promise<NextResponse<TenantInfo | ApiErrorResponse>>} JSON response with tenant info or error
 * 
 * @example Response (Success - 200):
 * ```json
 * {
 *   "slug": "clinic-a",
 *   "themeId": "clx123"
 * }
 * ```
 * 
 * @example Response (No Header - 400):
 * ```json
 * {
 *   "error": "No tenant header found"
 * }
 * ```
 * 
 * @example Response (Not Found - 404):
 * ```json
 * {
 *   "error": "Tenant not found"
 * }
 * ```
 */
export async function GET(
  request: NextRequest
): Promise<NextResponse<TenantInfo | ApiErrorResponse>> {
  try {
    // Read x-tenant header from request
    const tenantSlug = request.headers.get(TENANT_HEADER)
    
    if (!tenantSlug) {
      return NextResponse.json(
        { error: 'No tenant header found' },
        { status: 400 }
      )
    }
    
    // Query tenant by slug with themeId
    const tenantResult = await dbQuery<TenantInfo>(
      'SELECT slug, "themeId" as "themeId" FROM tenants WHERE slug = $1 LIMIT 1',
      [tenantSlug]
    )
    const tenant = tenantResult.rows[0]
    
    // Add error handling for missing tenant
    if (!tenant) {
      return NextResponse.json(
        { error: 'Tenant not found' },
        { status: 404 }
      )
    }
    
    // Return tenant slug and themeId
    return NextResponse.json(tenant)
  } catch (error) {
    console.error('Error fetching tenant:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}
