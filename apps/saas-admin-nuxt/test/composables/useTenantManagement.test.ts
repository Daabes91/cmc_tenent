import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useTenantManagement } from '~/composables/useTenantManagement'

// Mock dependencies
const mockPush = vi.fn()
const mockRoute = {
  query: {}
}

vi.mock('#app', () => ({
  useRouter: () => ({
    push: mockPush
  }),
  useRoute: () => mockRoute,
  reactive: (obj: any) => obj,
  readonly: (obj: any) => obj
}))

vi.mock('~/composables/useSaasApi', () => ({
  useSaasApi: () => ({
    getTenants: vi.fn()
  })
}))

vi.mock('~/composables/useApiCache', () => ({
  useApiCache: () => ({
    cachedCall: vi.fn((fn) => fn()),
    generateKey: vi.fn(),
    DEFAULT_TTL: { tenantList: 60000 }
  })
}))

vi.mock('~/composables/useApiErrorHandler', () => ({
  useApiErrorHandler: () => ({
    handleError: vi.fn()
  })
}))

vi.mock('~/composables/useDebounce', () => ({
  useDebounce: () => ({
    debounce: (fn: Function) => fn
  })
}))

describe('useTenantManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockRoute.query = {}
  })

  describe('fetchTenants', () => {
    it('should fetch tenants successfully', async () => {
      const mockTenants = {
        content: [
          { id: 1, slug: 'clinic1', name: 'Clinic 1', status: 'ACTIVE', createdAt: '2024-01-01' },
          { id: 2, slug: 'clinic2', name: 'Clinic 2', status: 'ACTIVE', createdAt: '2024-01-02' }
        ],
        totalPages: 1,
        totalElements: 2
      }

      const { useSaasApi } = await import('~/composables/useSaasApi')
      const api = useSaasApi()
      vi.mocked(api.getTenants).mockResolvedValueOnce(mockTenants)

      const tenantMgmt = useTenantManagement()
      await tenantMgmt.fetchTenants()

      expect(tenantMgmt.state.tenants).toEqual(mockTenants.content)
      expect(tenantMgmt.state.totalPages).toBe(1)
      expect(tenantMgmt.state.totalElements).toBe(2)
      expect(tenantMgmt.state.loading).toBe(false)
      expect(tenantMgmt.state.error).toBe(null)
    })

    it('should handle fetch errors', async () => {
      const { useSaasApi } = await import('~/composables/useSaasApi')
      const api = useSaasApi()
      vi.mocked(api.getTenants).mockRejectedValueOnce(new Error('Network error'))

      const tenantMgmt = useTenantManagement()
      await tenantMgmt.fetchTenants()

      expect(tenantMgmt.state.error).toBe('Network error')
      expect(tenantMgmt.state.loading).toBe(false)
    })
  })

  describe('setSearch', () => {
    it('should update search and reset page', () => {
      const tenantMgmt = useTenantManagement()
      tenantMgmt.state.page = 3

      tenantMgmt.setSearch('test clinic')

      expect(tenantMgmt.state.search).toBe('test clinic')
      expect(tenantMgmt.state.page).toBe(1)
    })
  })

  describe('setStatus', () => {
    it('should update status filter and reset page', () => {
      const tenantMgmt = useTenantManagement()
      tenantMgmt.state.page = 2

      tenantMgmt.setStatus('INACTIVE')

      expect(tenantMgmt.state.status).toBe('INACTIVE')
      expect(tenantMgmt.state.page).toBe(1)
    })
  })

  describe('setSort', () => {
    it('should toggle sort direction for same column', () => {
      const tenantMgmt = useTenantManagement()
      tenantMgmt.state.sortBy = 'name'
      tenantMgmt.state.sortDirection = 'asc'

      tenantMgmt.setSort('name')

      expect(tenantMgmt.state.sortDirection).toBe('desc')
    })

    it('should set new column with asc direction', () => {
      const tenantMgmt = useTenantManagement()
      tenantMgmt.state.sortBy = 'name'
      tenantMgmt.state.sortDirection = 'desc'

      tenantMgmt.setSort('createdAt')

      expect(tenantMgmt.state.sortBy).toBe('createdAt')
      expect(tenantMgmt.state.sortDirection).toBe('asc')
    })
  })

  describe('setPage', () => {
    it('should update current page', () => {
      const tenantMgmt = useTenantManagement()
      
      tenantMgmt.setPage(3)

      expect(tenantMgmt.state.page).toBe(3)
    })
  })

  describe('setPageSize', () => {
    it('should update page size and reset to first page', () => {
      const tenantMgmt = useTenantManagement()
      tenantMgmt.state.page = 5

      tenantMgmt.setPageSize(50)

      expect(tenantMgmt.state.pageSize).toBe(50)
      expect(tenantMgmt.state.page).toBe(1)
    })
  })

  describe('viewTenant', () => {
    it('should navigate to tenant detail page', () => {
      const tenantMgmt = useTenantManagement()
      
      tenantMgmt.viewTenant(123)

      expect(mockPush).toHaveBeenCalledWith('/tenants/123')
    })
  })

  describe('createTenant', () => {
    it('should navigate to create tenant page', () => {
      const tenantMgmt = useTenantManagement()
      
      tenantMgmt.createTenant()

      expect(mockPush).toHaveBeenCalledWith('/tenants/new')
    })
  })
})
