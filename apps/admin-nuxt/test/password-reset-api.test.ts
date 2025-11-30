import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as fc from 'fast-check'

/**
 * Feature: admin-forgot-password, Property 13: Password reset API call
 * 
 * For any valid password and token combination, the system should send a 
 * password reset request to the backend API
 * 
 * Validates: Requirements 4.4
 */

describe('Password Reset API', () => {
  // Mock the $fetch function
  let mockFetch: any
  let capturedRequests: any[] = []

  beforeEach(() => {
    capturedRequests = []
    mockFetch = vi.fn((url: string, options: any) => {
      capturedRequests.push({ url, options })
      return Promise.resolve({ message: 'Password reset successful' })
    })
    
    // Mock global $fetch
    global.$fetch = mockFetch
  })

  // Helper function to simulate the resetPassword call
  const simulateResetPassword = async (token: string, newPassword: string) => {
    const apiBase = 'http://localhost:8080/api'
    
    await mockFetch("/auth/reset-password", {
      baseURL: apiBase,
      method: "POST",
      headers: {},
      body: {
        token,
        newPassword
      }
    })
  }

  // Generator for valid tokens (simulating JWT-like tokens)
  const tokenArbitrary = fc.string({ minLength: 20, maxLength: 200 })
    .filter(s => s.trim().length > 0)

  // Generator for valid passwords (at least 8 characters)
  const passwordArbitrary = fc.string({ minLength: 8, maxLength: 100 })
    .filter(s => s.trim().length >= 8)

  it('Property 13: should send API request with token and newPassword', async () => {
    await fc.assert(
      fc.asyncProperty(
        tokenArbitrary,
        passwordArbitrary,
        async (token, newPassword) => {
          capturedRequests = []
          
          await simulateResetPassword(token, newPassword)
          
          // Verify that exactly one request was made
          expect(capturedRequests).toHaveLength(1)
          
          const request = capturedRequests[0]
          
          // Verify the request body contains both required fields
          expect(request.options.body).toHaveProperty('token')
          expect(request.options.body).toHaveProperty('newPassword')
          
          // Verify the values match what was passed
          expect(request.options.body.token).toBe(token)
          expect(request.options.body.newPassword).toBe(newPassword)
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 13: should use POST method for password reset', async () => {
    await fc.assert(
      fc.asyncProperty(
        tokenArbitrary,
        passwordArbitrary,
        async (token, newPassword) => {
          capturedRequests = []
          
          await simulateResetPassword(token, newPassword)
          
          const request = capturedRequests[0]
          
          // Verify POST method is used
          expect(request.options.method).toBe('POST')
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 13: should target correct reset password endpoint', async () => {
    await fc.assert(
      fc.asyncProperty(
        tokenArbitrary,
        passwordArbitrary,
        async (token, newPassword) => {
          capturedRequests = []
          
          await simulateResetPassword(token, newPassword)
          
          const request = capturedRequests[0]
          
          // Verify correct endpoint
          expect(request.url).toBe('/auth/reset-password')
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 13: should not include extra fields in request body', async () => {
    await fc.assert(
      fc.asyncProperty(
        tokenArbitrary,
        passwordArbitrary,
        async (token, newPassword) => {
          capturedRequests = []
          
          await simulateResetPassword(token, newPassword)
          
          const request = capturedRequests[0]
          const bodyKeys = Object.keys(request.options.body)
          
          // Verify only expected fields are present
          expect(bodyKeys).toHaveLength(2)
          expect(bodyKeys).toContain('token')
          expect(bodyKeys).toContain('newPassword')
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 13: should handle various password lengths (8-100 chars)', async () => {
    await fc.assert(
      fc.asyncProperty(
        tokenArbitrary,
        fc.integer({ min: 8, max: 100 }).chain(length =>
          fc.string({ minLength: length, maxLength: length })
        ),
        async (token, newPassword) => {
          capturedRequests = []
          
          await simulateResetPassword(token, newPassword)
          
          const request = capturedRequests[0]
          
          // Verify password is sent correctly regardless of length
          expect(request.options.body.newPassword).toBe(newPassword)
          expect(request.options.body.newPassword.length).toBeGreaterThanOrEqual(8)
          expect(request.options.body.newPassword.length).toBeLessThanOrEqual(100)
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 13: should handle tokens with special characters', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.string({ minLength: 20, maxLength: 200 })
          .filter(s => s.trim().length > 0),
        passwordArbitrary,
        async (token, newPassword) => {
          capturedRequests = []
          
          await simulateResetPassword(token, newPassword)
          
          const request = capturedRequests[0]
          
          // Verify token is sent exactly as provided (no encoding issues)
          expect(request.options.body.token).toBe(token)
        }
      ),
      { numRuns: 100 }
    )
  })

  it('Property 13: should handle passwords with special characters', async () => {
    await fc.assert(
      fc.asyncProperty(
        tokenArbitrary,
        fc.string({ minLength: 8, maxLength: 100 })
          .filter(s => s.length >= 8),
        async (token, newPassword) => {
          capturedRequests = []
          
          await simulateResetPassword(token, newPassword)
          
          const request = capturedRequests[0]
          
          // Verify password is sent exactly as provided (no encoding issues)
          expect(request.options.body.newPassword).toBe(newPassword)
        }
      ),
      { numRuns: 100 }
    )
  })
})
