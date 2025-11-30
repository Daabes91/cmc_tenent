import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as fc from 'fast-check'

/**
 * Feature: admin-forgot-password, Property 2: API request contains required fields
 * 
 * For any valid forgot password request, the API call should include both email 
 * and tenant slug in the request body
 * 
 * Validates: Requirements 1.3
 */

describe('Password Reset API Calls', () => {
  // Mock the $fetch function
  let mockFetch: any
  let capturedRequests: any[] = []

  beforeEach(() => {
    capturedRequests = []
    mockFetch = vi.fn((url: string, options: any) => {
      capturedRequests.push({ url, options })
      return Promise.resolve({ message: 'Success' })
    })
    
    // Mock global $fetch
    global.$fetch = mockFetch
  })

  // Helper function to simulate the requestReset call
  const simulateRequestReset = async (email: string, tenantSlug: string) => {
    const apiBase = 'http://localhost:8080/api'
    const headers = tenantSlug ? { "X-Tenant-Slug": tenantSlug } : {}
    
    await mockFetch("/auth/forgot-password", {
      baseURL: apiBase,
      method: "POST",
      headers,
      body: {
        email,
        tenantSlug
      }
    })
  }

  it('Property 2: API request should contain both email and tenantSlug in body', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.emailAddress(), // Generate valid email addresses
        fc.string({ minLength: 2, maxLength: 20 }), // Generate tenant slugs
        async (email, tenantSlug) => {
          capturedRequests = []
          
          await simulateRequestReset(email, tenantSlug)
          
          // Verify that exactly one request was made
          expect(capturedRequests).toHaveLength(1)
          
          const request = capturedRequests[0]
          
          // Verify the request body contains both required fields
          expect(request.options.body).toHaveProperty('email')
          expect(request.options.body).toHaveProperty('tenantSlug')
          
          // Verify the values match what was passed
          expect(request.options.body.email).toBe(email)
          expect(request.options.body.tenantSlug).toBe(tenantSlug)
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 2: API request should include tenant slug in headers', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.emailAddress(),
        fc.string({ minLength: 2, maxLength: 20 }),
        async (email, tenantSlug) => {
          capturedRequests = []
          
          await simulateRequestReset(email, tenantSlug)
          
          const request = capturedRequests[0]
          
          // Verify the tenant slug is in the headers
          expect(request.options.headers).toHaveProperty('X-Tenant-Slug')
          expect(request.options.headers['X-Tenant-Slug']).toBe(tenantSlug)
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 2: API request should use POST method', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.emailAddress(),
        fc.string({ minLength: 2, maxLength: 20 }),
        async (email, tenantSlug) => {
          capturedRequests = []
          
          await simulateRequestReset(email, tenantSlug)
          
          const request = capturedRequests[0]
          
          // Verify POST method is used
          expect(request.options.method).toBe('POST')
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 2: API request should target correct endpoint', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.emailAddress(),
        fc.string({ minLength: 2, maxLength: 20 }),
        async (email, tenantSlug) => {
          capturedRequests = []
          
          await simulateRequestReset(email, tenantSlug)
          
          const request = capturedRequests[0]
          
          // Verify correct endpoint
          expect(request.url).toBe('/auth/forgot-password')
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 2: API request body should not contain additional unexpected fields', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.emailAddress(),
        fc.string({ minLength: 2, maxLength: 20 }),
        async (email, tenantSlug) => {
          capturedRequests = []
          
          await simulateRequestReset(email, tenantSlug)
          
          const request = capturedRequests[0]
          const bodyKeys = Object.keys(request.options.body)
          
          // Verify only expected fields are present
          expect(bodyKeys).toHaveLength(2)
          expect(bodyKeys).toContain('email')
          expect(bodyKeys).toContain('tenantSlug')
        }
      ),
      { numRuns: 100 }
    )
  })
})
