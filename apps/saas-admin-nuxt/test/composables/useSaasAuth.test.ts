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

describe('useSaasAuth', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('login', () => {
    it('should successfully login and store credentials', async () => {
      const mockResponse = {
        token: 'test-token-123',
        managerName: 'Test Manager',
        expiresAt: new Date(Date.now() + 3600000).toISOString()
      }

      mockFetch.mockResolvedValueOnce(mockResponse)

      const auth = useSaasAuth()
      await auth.login('test@example.com', 'password123')

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/saas/auth/login',
        {
          method: 'POST',
          body: {
            email: 'test@example.com',
            password: 'password123'
          }
        }
      )

      expect(localStorage.setItem).toHaveBeenCalledWith('saas_auth_token', mockResponse.token)
      expect(localStorage.setItem).toHaveBeenCalledWith('saas_manager_name', mockResponse.managerName)
      expect(localStorage.setItem).toHaveBeenCalledWith('saas_token_expiry', mockResponse.expiresAt)
      expect(auth.isAuthenticated.value).toBe(true)
      expect(auth.managerName.value).toBe('Test Manager')
    })

    it('should throw error on failed login', async () => {
      mockFetch.mockRejectedValueOnce({
        data: { message: 'Invalid credentials' }
      })

      const auth = useSaasAuth()
      
      await expect(auth.login('test@example.com', 'wrong')).rejects.toThrow('Invalid credentials')
      expect(auth.isAuthenticated.value).toBe(false)
    })
  })

  describe('logout', () => {
    it('should clear auth state and storage', () => {
      const auth = useSaasAuth()
      
      // Simulate logged in state
      localStorage.setItem('saas_auth_token', 'test-token')
      localStorage.setItem('saas_manager_name', 'Test Manager')
      localStorage.setItem('saas_token_expiry', new Date(Date.now() + 3600000).toISOString())

      auth.logout()

      expect(localStorage.removeItem).toHaveBeenCalledWith('saas_auth_token')
      expect(localStorage.removeItem).toHaveBeenCalledWith('saas_manager_name')
      expect(localStorage.removeItem).toHaveBeenCalledWith('saas_token_expiry')
      expect(auth.isAuthenticated.value).toBe(false)
      expect(auth.managerName.value).toBe(null)
    })
  })

  describe('checkAuth', () => {
    it('should return true for valid token', () => {
      const futureDate = new Date(Date.now() + 3600000).toISOString()
      
      localStorage.getItem.mockImplementation((key: string) => {
        if (key === 'saas_auth_token') return 'test-token'
        if (key === 'saas_manager_name') return 'Test Manager'
        if (key === 'saas_token_expiry') return futureDate
        return null
      })

      const auth = useSaasAuth()
      const isAuth = auth.checkAuth()

      expect(isAuth).toBe(true)
    })

    it('should return false for expired token', () => {
      const pastDate = new Date(Date.now() - 3600000).toISOString()
      
      localStorage.getItem.mockImplementation((key: string) => {
        if (key === 'saas_auth_token') return 'test-token'
        if (key === 'saas_manager_name') return 'Test Manager'
        if (key === 'saas_token_expiry') return pastDate
        return null
      })

      const auth = useSaasAuth()
      const isAuth = auth.checkAuth()

      expect(isAuth).toBe(false)
      expect(localStorage.removeItem).toHaveBeenCalled()
    })

    it('should return false when no token exists', () => {
      const auth = useSaasAuth()
      const isAuth = auth.checkAuth()

      expect(isAuth).toBe(false)
    })
  })

  describe('getToken', () => {
    it('should return stored token', () => {
      localStorage.getItem.mockImplementation((key: string) => {
        if (key === 'saas_auth_token') return 'test-token-123'
        return null
      })

      const auth = useSaasAuth()
      const token = auth.getToken()

      expect(token).toBe('test-token-123')
    })

    it('should return null when no token exists', () => {
      const auth = useSaasAuth()
      const token = auth.getToken()

      expect(token).toBe(null)
    })
  })

  describe('updateToken', () => {
    it('should update token and expiry', () => {
      const auth = useSaasAuth()
      const newToken = 'new-token-456'
      const newExpiry = new Date(Date.now() + 7200000).toISOString()

      auth.updateToken(newToken, newExpiry)

      expect(localStorage.setItem).toHaveBeenCalledWith('saas_auth_token', newToken)
      expect(localStorage.setItem).toHaveBeenCalledWith('saas_token_expiry', newExpiry)
      expect(auth.getToken()).toBe(newToken)
    })
  })
})
