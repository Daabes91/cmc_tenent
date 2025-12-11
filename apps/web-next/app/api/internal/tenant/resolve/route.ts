import { NextRequest, NextResponse } from 'next/server'
import { dbQuery } from '@/lib/db'
import { normalizeDomain, sanitizeTenantSlug } from '@/lib/tenant'

type TenantDomainRecord = {
  slug: string
  status?: string | null
}

async function findTenantByDomain(domain: string): Promise<TenantDomainRecord | undefined> {
  const columns = ['custom_domain', 'domain']

  for (const column of columns) {
    try {
      const result = await dbQuery<TenantDomainRecord>(
        `SELECT slug, status FROM tenants WHERE LOWER("${column}") = LOWER($1) LIMIT 1`,
        [domain]
      )
      if (result.rows[0]) {
        return result.rows[0]
      }
    } catch (error) {
      // Ignore missing column errors so we can fall back to the next column
      if (process.env.NODE_ENV !== 'production') {
        console.debug(`Tenant domain lookup skipped column ${column}:`, (error as Error)?.message)
      }
      continue
    }
  }

  return undefined
}

export async function GET(request: NextRequest) {
  const domainParam =
    request.nextUrl.searchParams.get('domain') ||
    request.nextUrl.searchParams.get('host')

  const domain = normalizeDomain(domainParam)

  if (!domain) {
    return NextResponse.json(
      { error: 'Missing domain parameter' },
      { status: 400 }
    )
  }

  try {
    const tenant = await findTenantByDomain(domain)

    if (!tenant) {
      return NextResponse.json({ error: 'Tenant not found' }, { status: 404 })
    }

    // Treat inactive tenants as not found for routing purposes
    if (tenant.status && tenant.status.toUpperCase() !== 'ACTIVE') {
      return NextResponse.json(
        { error: 'Tenant is not active' },
        { status: 404 }
      )
    }

    const slug = sanitizeTenantSlug(tenant.slug)
    if (!slug) {
      return NextResponse.json({ error: 'Invalid tenant slug' }, { status: 404 })
    }

    return NextResponse.json({ slug })
  } catch (error) {
    console.error('Error resolving tenant by domain:', error)
    return NextResponse.json(
      { error: 'Failed to resolve tenant' },
      { status: 500 }
    )
  }
}
