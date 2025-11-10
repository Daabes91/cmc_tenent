import { NextResponse } from 'next/server'
import { prisma } from '@/lib/prisma'
import type { ThemeListItem, ApiErrorResponse } from '@/lib/theme-types'

/**
 * GET /api/themes
 * 
 * Returns all published themes available for tenant selection.
 * This endpoint is used by the theme settings page to populate the theme selection dropdown.
 * 
 * @returns {Promise<NextResponse<ThemeListItem[] | ApiErrorResponse>>} JSON response with theme list or error
 * 
 * @example Response (Success - 200):
 * ```json
 * [
 *   { "id": "clx123", "key": "clinic", "name": "Clinic Theme" },
 *   { "id": "clx456", "key": "barber", "name": "Barber Theme" }
 * ]
 * ```
 * 
 * @example Response (Error - 500):
 * ```json
 * {
 *   "error": "Failed to fetch themes",
 *   "message": "Database connection failed"
 * }
 * ```
 */
export async function GET(): Promise<NextResponse<ThemeListItem[] | ApiErrorResponse>> {
  try {
    const themes = await prisma.theme.findMany({
      where: { status: 'published' },
      select: { 
        id: true, 
        key: true, 
        name: true 
      },
      orderBy: {
        name: 'asc'
      }
    })
    
    return NextResponse.json(themes)
  } catch (error) {
    console.error('Error fetching themes:', error)
    
    return NextResponse.json(
      { 
        error: 'Failed to fetch themes',
        message: error instanceof Error ? error.message : 'Unknown error'
      },
      { status: 500 }
    )
  }
}
