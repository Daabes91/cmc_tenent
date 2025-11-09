// SAAS Manager Authentication Composable
import { ref, computed } from 'vue'

interface SaasAuthState {
  token: string | null
  managerName: string | null
  isAuthenticated: boolean
}

interface LoginResponse {
  tokenType: string
  accessToken: string
  expiresAt: number
  managerId: number
  email: string
  fullName: string
}

const TOKEN_KEY = 'saas_auth_token'
const MANAGER_NAME_KEY = 'saas_manager_name'
const TOKEN_EXPIRY_KEY = 'saas_token_expiry'

// Reactive state
const authState = ref<SaasAuthState>({
  token: null,
  managerName: null,
  isAuthenticated: false
})

export const useSaasAuth = () => {
  const config = useRuntimeConfig()

  // Initialize auth state from localStorage
  const initializeAuth = () => {
    if (import.meta.client) {
      const token = localStorage.getItem(TOKEN_KEY)
      const managerName = localStorage.getItem(MANAGER_NAME_KEY)
      const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY)

      if (token && managerName && expiry) {
        const expiryDate = new Date(expiry)
        const now = new Date()

        if (expiryDate > now) {
          authState.value = {
            token,
            managerName,
            isAuthenticated: true
          }
        } else {
          // Token expired, clear storage
          clearAuthStorage()
        }
      }
    }
  }

  // Clear auth storage
  const clearAuthStorage = () => {
    if (import.meta.client) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(MANAGER_NAME_KEY)
      localStorage.removeItem(TOKEN_EXPIRY_KEY)
    }
  }

  // Login method
  const login = async (email: string, password: string): Promise<void> => {
    try {
      // Clear any existing auth before login
      clearAuthStorage()
      
      const response = await $fetch<LoginResponse>(`${config.public.saasApiBase}/auth/login`, {
        method: 'POST',
        body: {
          email,
          password
        }
      })

      // Convert expiresAt timestamp to ISO string
      const expiryDate = new Date(response.expiresAt * 1000).toISOString()

      // Store token and manager info
      if (import.meta.client) {
        localStorage.setItem(TOKEN_KEY, response.accessToken)
        localStorage.setItem(MANAGER_NAME_KEY, response.fullName)
        localStorage.setItem(TOKEN_EXPIRY_KEY, expiryDate)
        
        // Debug: Log token audience
        try {
          const payload = JSON.parse(atob(response.accessToken.split('.')[1]))
          console.log('✅ New token stored with audience:', payload.aud)
        } catch (e) {
          console.warn('Could not decode token for debugging')
        }
      }

      // Update state
      authState.value = {
        token: response.accessToken,
        managerName: response.fullName,
        isAuthenticated: true
      }
    } catch (error: any) {
      console.error('Login failed:', error)
      throw new Error(error.data?.message || 'Login failed. Please check your credentials.')
    }
  }

  // Logout method
  const logout = () => {
    clearAuthStorage()
    authState.value = {
      token: null,
      managerName: null,
      isAuthenticated: false
    }
  }

  // Check authentication status
  const checkAuth = (): boolean => {
    if (!authState.value.isAuthenticated) {
      initializeAuth()
    }

    // Validate token expiration
    if (authState.value.isAuthenticated && import.meta.client) {
      const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY)
      if (expiry) {
        const expiryDate = new Date(expiry)
        const now = new Date()

        if (expiryDate <= now) {
          logout()
          return false
        }
      }
    }

    return authState.value.isAuthenticated
  }

  // Get token
  const getToken = (): string | null => {
    if (!authState.value.token) {
      initializeAuth()
    }
    return authState.value.token
  }

  // Get token expiry
  const getTokenExpiry = (): Date | null => {
    if (!import.meta.client) return null
    
    const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY)
    return expiry ? new Date(expiry) : null
  }

  // Update token (used by refresh mechanism)
  const updateToken = (token: string, expiresAt: string) => {
    if (import.meta.client) {
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(TOKEN_EXPIRY_KEY, expiresAt)
    }

    authState.value.token = token
  }

  // Computed properties
  const isAuthenticated = computed(() => authState.value.isAuthenticated)
  const managerName = computed(() => authState.value.managerName)

  // Initialize on composable creation
  if (import.meta.client && !authState.value.isAuthenticated) {
    initializeAuth()
  }

  return {
    // State
    isAuthenticated,
    managerName,
    
    // Methods
    login,
    logout,
    checkAuth,
    getToken,
    getTokenExpiry,
    updateToken
  }
}
