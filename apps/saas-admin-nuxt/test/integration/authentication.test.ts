import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useSaasAuth } from '~/composables/useSaasAuth'

// Mock $fetch
const mockFetch = vi.fn()
global.$fetch = mockFetch as any

// Mock useRuntimeConfig
vi.mock('#app', () => ({
  useRuntimeConfig: () => ({
    public: {
      saasApiBase: 'http://localhost:8080/saas'
    }
  })
}))

describe('Authentication Flow Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('should complete full login flow', async () => {
    const mockResponse = {
      token: 'test-jwt-token',
      managerName: 'John Doe',
      expiresAt: new Date(Date.now() + 3600000).toISOString()
    }

    mockFetch.mockResolvedValueOnce(mockResponse)

    const auth = useSaasAuth()
    
    // Initial state - not authenticated
    expect(auth.isAuthenticated.value).toBe(false)
    expect(auth.managerName.value).toBe(null)

    // Perform login
    await auth.login('admin@example.com', 'password123')

    // Verify authenticated state
    expect(auth.isAuthenticated.value).toBe(true)
    expect(auth.managerName.value).toBe('John Doe')
    expect(auth.getToken()).toBe('test-jwt-token')

    // Verify localStorage was updated
    expect(localStorage.setItem).toHaveBeenCalledWith('saas_auth_token', mockResponse.token)
    expect(localStorage.setItem).toHaveBeenCalledWith('saas_manager_name', mockResponse.managerName)
    expect(localStorage.setItem).toHaveBeenCalledWith('saas_token_expiry', mockResponse.expiresAt)
  })

  it('should handle login failure', async () => {
    mockFetch.mockRejectedValueOnce({
      data: { message: 'Invalid credentials' }
    })

    const auth = useSaasAuth()

    await expect(auth.login('wrong@example.com', 'wrongpass')).rejects.toThrow('Invalid credentials')
    
    expect(auth.isAuthenticated.value).toBe(false)
    expect(auth.getToken()).toBe(null)
  })

  it('should restore session from localStorage', () => {
    const futureDate = new Date(Date.now() + 3600000).toISOString()
    
    localStorage.getItem.mockImplementation((key: string) => {
      if (key === 'saas_auth_token') return 'stored-token'
      if (key === 'saas_manager_name') return 'Stored Manager'
      if (key === 'saas_token_expiry') return futureDate
      return null
    })

    const auth = useSaasAuth()
    const isAuth = auth.checkAuth()

    expect(isAuth).toBe(true)
    expect(auth.isAuthenticated.value).toBe(true)
    expect(auth.managerName.value).toBe('Stored Manager')
    expect(auth.getToken()).toBe('stored-token')
  })

  it('should handle expired token', () => {
    const pastDate = new Date(Date.now() - 3600000).toISOString()
    
    localStorage.getItem.mockImplementation((key: string) => {
      if (key === 'saas_auth_token') return 'expired-token'
      if (key === 'saas_manager_name') return 'Expired Manager'
      if (key === 'saas_token_expiry') return pastDate
      return null
    })

    const auth = useSaasAuth()
    const isAuth = auth.checkAuth()

    expect(isAuth).toBe(false)
    expect(auth.isAuthenticated.value).toBe(false)
    expect(localStorage.removeItem).toHaveBeenCalled()
  })

  it('should complete logout flow', () => {
    const futureDate = new Date(Date.now() + 3600000).toISOString()
    
    localStorage.getItem.mockImplementation((key: string) => {
      if (key === 'saas_auth_token') return 'test-token'
      if (key === 'saas_manager_name') return 'Test Manager'
      if (key === 'saas_token_expiry') return futureDate
      return null
    })

    const auth = useSaasAuth()
    auth.checkAuth()

    expect(auth.isAuthenticated.value).toBe(true)

    auth.logout()

    expect(auth.isAuthenticated.value).toBe(false)
    expect(auth.managerName.value).toBe(null)
    expect(auth.getToken()).toBe(null)
    expect(localStorage.removeItem).toHaveBeenCalledWith('saas_auth_token')
    expect(localStorage.removeItem).toHaveBeenCalledWith('saas_manager_name')
    expect(localStorage.removeItem).toHaveBeenCalledWith('saas_token_expiry')
  })

  it('should update token during refresh', () => {
    const auth = useSaasAuth()
    const newToken = 'refreshed-token'
    const newExpiry = new Date(Date.now() + 7200000).toISOString()

    auth.updateToken(newToken, newExpiry)

    expect(localStorage.setItem).toHaveBeenCalledWith('saas_auth_token', newToken)
    expect(localStorage.setItem).toHaveBeenCalledWith('saas_token_expiry', newExpiry)
    expect(auth.getToken()).toBe(newToken)
  })
})
