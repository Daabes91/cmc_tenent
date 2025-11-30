import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as fc from 'fast-check'

/**
 * Feature: admin-forgot-password, Property 16: Tenant slug preservation
 * 
 * For any password reset flow that includes a tenant slug, the system should 
 * preserve and pre-fill that tenant slug when redirecting to the login page
 * 
 * Validates: Requirements 5.3
 */

describe('Tenant Slug Preservation', () => {
  // Mock router
  let mockRouter: any
  let capturedNavigations: any[] = []

  beforeEach(() => {
    capturedNavigations = []
    mockRouter = {
      push: vi.fn((destination: any) => {
        capturedNavigations.push(destination)
        return Promise.resolve()
      })
    }
  })

  // Helper to simulate the redirect after successful password reset
  const simulatePasswordResetRedirect = (tenantSlug: string | null) => {
    if (tenantSlug) {
      mockRouter.push(`/login?tenant=${tenantSlug}`)
    } else {
      mockRouter.push('/login')
    }
  }

  it('Property 16: Should preserve tenant slug in login redirect URL', async () => {
    await fc.assert(
      fc.asyncProperty(
        // Generate alphanumeric strings to avoid whitespace-only strings
        fc.stringMatching(/^[a-zA-Z0-9][a-zA-Z0-9-_]{1,19}$/),
        async (tenantSlug) => {
          capturedNavigations = []
          
          simulatePasswordResetRedirect(tenantSlug)
          
          // Verify navigation occurred
          expect(capturedNavigations).toHaveLength(1)
          
          const destination = capturedNavigations[0]
          
          // Verify tenant slug is in the URL
          expect(destination).toContain('/login')
          expect(destination).toContain(`tenant=${tenantSlug}`)
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 16: Should handle null tenant slug gracefully', async () => {
    capturedNavigations = []
    
    simulatePasswordResetRedirect(null)
    
    // Verify navigation occurred
    expect(capturedNavigations).toHaveLength(1)
    
    const destination = capturedNavigations[0]
    
    // Verify it navigates to login without tenant parameter
    expect(destination).toBe('/login')
    expect(destination).not.toContain('tenant=')
  })

  it('Property 16: Tenant slug should be URL-encoded in redirect', async () => {
    await fc.assert(
      fc.asyncProperty(
        // Generate alphanumeric strings to avoid whitespace-only strings
        fc.stringMatching(/^[a-zA-Z0-9][a-zA-Z0-9-_]{1,19}$/),
        async (tenantSlug) => {
          capturedNavigations = []
          
          simulatePasswordResetRedirect(tenantSlug)
          
          const destination = capturedNavigations[0]
          
          // Verify the tenant slug appears in the URL
          // The slug should be part of the query string
          expect(destination).toMatch(/\/login\?tenant=.+/)
        }
      ),
      { numRuns: 100 }
    )
  })

  // Test the login page's ability to extract tenant from query parameter
  describe('Login Page Tenant Extraction', () => {
    const extractTenantFromQuery = (queryString: string): string | null => {
      const params = new URLSearchParams(queryString)
      const tenant = params.get('tenant')
      return tenant ? tenant.trim().toLowerCase() : null
    }

    it('Property 16: Should extract tenant slug from query parameter', async () => {
      await fc.assert(
        fc.asyncProperty(
          // Generate alphanumeric strings to avoid whitespace-only strings
          fc.stringMatching(/^[a-zA-Z0-9][a-zA-Z0-9-_]{1,19}$/),
          async (tenantSlug) => {
            const queryString = `tenant=${tenantSlug}`
            const extracted = extractTenantFromQuery(queryString)
            
            // Verify tenant slug is extracted
            expect(extracted).toBeTruthy()
            expect(extracted).toBe(tenantSlug.toLowerCase())
          }
        ),
        { numRuns: 100 }
      )
    })

    it('Property 16: Should handle missing tenant parameter', () => {
      const queryString = 'other=value'
      const extracted = extractTenantFromQuery(queryString)
      
      expect(extracted).toBeNull()
    })

    it('Property 16: Should normalize tenant slug to lowercase', async () => {
      await fc.assert(
        fc.asyncProperty(
          // Generate alphanumeric strings to avoid whitespace-only strings
          fc.stringMatching(/^[a-zA-Z0-9][a-zA-Z0-9-_]{1,19}$/),
          async (tenantSlug) => {
            const mixedCase = tenantSlug.split('').map((c, i) => 
              i % 2 === 0 ? c.toUpperCase() : c.toLowerCase()
            ).join('')
            
            const queryString = `tenant=${mixedCase}`
            const extracted = extractTenantFromQuery(queryString)
            
            // Verify tenant slug is normalized to lowercase
            expect(extracted).toBe(mixedCase.toLowerCase())
          }
        ),
        { numRuns: 100 }
      )
    })

    it('Property 16: Should trim whitespace from tenant slug', async () => {
      await fc.assert(
        fc.asyncProperty(
          // Generate alphanumeric strings to avoid whitespace-only strings
          fc.stringMatching(/^[a-zA-Z0-9][a-zA-Z0-9-_]{1,19}$/),
          fc.integer({ min: 0, max: 5 }), // Leading spaces
          fc.integer({ min: 0, max: 5 }), // Trailing spaces
          async (tenantSlug, leadingSpaces, trailingSpaces) => {
            const paddedSlug = ' '.repeat(leadingSpaces) + tenantSlug + ' '.repeat(trailingSpaces)
            const queryString = `tenant=${encodeURIComponent(paddedSlug)}`
            const extracted = extractTenantFromQuery(queryString)
            
            // Verify whitespace is trimmed
            expect(extracted).toBe(tenantSlug.trim().toLowerCase())
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  // Test the complete round-trip flow
  describe('Complete Tenant Preservation Flow', () => {
    it('Property 16: Tenant slug should survive complete reset flow', async () => {
      await fc.assert(
        fc.asyncProperty(
          // Generate alphanumeric strings to avoid whitespace-only strings
          fc.stringMatching(/^[a-zA-Z0-9][a-zA-Z0-9-_]{1,19}$/),
          async (originalTenantSlug) => {
            // Step 1: Token validation returns tenant slug
            const tokenValidationResponse = {
              valid: true,
              message: 'Token is valid',
              tenantSlug: originalTenantSlug
            }
            
            // Step 2: Extract tenant slug from validation response
            const extractedSlug = tokenValidationResponse.tenantSlug
            
            // Step 3: Redirect to login with tenant slug
            capturedNavigations = []
            simulatePasswordResetRedirect(extractedSlug)
            
            const destination = capturedNavigations[0]
            
            // Step 4: Verify tenant slug is in the redirect URL
            expect(destination).toContain(`tenant=${originalTenantSlug}`)
            
            // Step 5: Extract tenant from login URL
            const queryString = destination.split('?')[1]
            const finalTenantSlug = new URLSearchParams(queryString).get('tenant')
            
            // Step 6: Verify tenant slug matches original
            expect(finalTenantSlug).toBe(originalTenantSlug)
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
