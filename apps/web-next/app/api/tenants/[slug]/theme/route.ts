import { NextRequest, NextResponse } from 'next/server'
import { dbQuery } from '@/lib/db'
import { z } from 'zod'
import type { UpdateThemeRequest, UpdateThemeResponse, ApiErrorResponse } from '@/lib/theme-types'

/**
 * POST /api/tenants/[slug]/theme
 * 
 * Updates a tenant's theme selection. This endpoint allows tenant administrators
 * to change their website's theme by providing a valid theme ID.
 * 
 * The theme change takes effect immediately without requiring application redeployment.
 * The client should reload the page after a successful update to apply the new theme.
 * 
 * @param {NextRequest} request - Next.js request object containing the theme update data
 * @param {Object} params - Route parameters
 * @param {string} params.slug - The tenant's unique slug identifier
 * @returns {Promise<NextResponse<UpdateThemeResponse | ApiErrorResponse>>} Updated tenant data or error
 * 
 * @example Request Body:
 * ```json
 * {
 *   "themeId": "clx123"
 * }
 * ```
 * 
 * @example Response (Success - 200):
 * ```json
 * {
 *   "id": 1,
 *   "slug": "clinic-a",
 *   "domain": null,
 *   "status": "ACTIVE",
 *   "themeId": "clx123",
 *   "theme": {
 *     "id": "clx123",
 *     "key": "clinic",
 *     "name": "Clinic Theme"
 *   },
 *   "updatedAt": "2024-01-15T10:30:00Z"
 * }
 * ```
 * 
 * @example Response (Validation Error - 400):
 * ```json
 * {
 *   "error": "Validation failed",
 *   "details": [...]
 * }
 * ```
 * 
 * @example Response (Theme Not Found - 404):
 * ```json
 * {
 *   "error": "Theme not found"
 * }
 * ```
 * 
 * @todo Add authentication and authorization checks
 * @todo Implement role-based access control (tenant admin only)
 * @todo Add audit logging for theme changes
 */

/**
 * Zod validation schema for theme update request
 * Ensures the request body contains a valid theme ID
 */
const updateThemeSchema = z.object({
  themeId: z.string().min(1, 'Theme ID is required')
}) satisfies z.ZodType<UpdateThemeRequest>

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ slug: string }> }
) {
  try {
    // TODO: Add authentication and authorization checks
    // - Verify user is authenticated
    // - Verify user has permission to update this tenant's theme
    // - Consider role-based access control (tenant admin only)
    
    const { slug } = await params
    
    // Parse and validate request body
    const body = await request.json()
    const validationResult = updateThemeSchema.safeParse(body)
    
    if (!validationResult.success) {
      return NextResponse.json(
        { 
          error: 'Validation failed',
          details: validationResult.error.issues
        },
        { status: 400 }
      )
    }
    
    const { themeId } = validationResult.data
    
    // Verify theme exists before assignment
    const themeResult = await dbQuery<{ id: string; key: string; name: string; status: string }>(
      'SELECT id, "key" as key, name, status FROM "Theme" WHERE id = $1',
      [themeId]
    )
    const theme = themeResult.rows[0]
    
    if (!theme) {
      return NextResponse.json(
        { error: 'Theme not found' },
        { status: 404 }
      )
    }
    
    // Verify theme is published
    if (theme.status !== 'published') {
      return NextResponse.json(
        { error: 'Theme is not available for selection' },
        { status: 400 }
      )
    }
    
    // Update tenant themeId in database
    const updatedTenantResult = await dbQuery<{
      id: string
      slug: string
      domain: string | null
      status: string
      themeId: string | null
      updatedAt: string
    }>(
      `
        UPDATE tenants
        SET "themeId" = $1, updated_at = NOW()
        WHERE slug = $2
        RETURNING id, slug, domain, status, "themeId" as "themeId", updated_at as "updatedAt"
      `,
      [themeId, slug]
    )
    
    const updatedTenant = updatedTenantResult.rows[0]
    
    if (!updatedTenant) {
      return NextResponse.json(
        { error: 'Tenant not found' },
        { status: 404 }
      )
    }
    
    // Return updated tenant data
    return NextResponse.json({
      ...updatedTenant,
      theme: {
        id: theme.id,
        key: theme.key,
        name: theme.name
      }
    })
    
  } catch (error) {
    console.error('Error updating tenant theme:', error)
    
    // Handle Prisma-specific errors
    if (error instanceof Error && error.message.includes('Record to update not found')) {
      return NextResponse.json(
        { error: 'Tenant not found' },
        { status: 404 }
      )
    }
    
    return NextResponse.json(
      { 
        error: 'Failed to update tenant theme',
        message: error instanceof Error ? error.message : 'Unknown error'
      },
      { status: 500 }
    )
  }
}
