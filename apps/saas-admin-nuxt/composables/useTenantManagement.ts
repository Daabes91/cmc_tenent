// Tenant Management Composable
import type { Tenant } from '~/types'
import { useApiErrorHandler } from './useApiErrorHandler'

interface TenantListState {
  tenants: Tenant[]
  loading: boolean
  error: string | null
  page: number
  totalPages: number
  totalElements: number
  pageSize: number
  search: string
  status: string
  sortBy: string
  sortDirection: 'asc' | 'desc'
}

export const useTenantManagement = () => {
  const api = useSaasApi()
  const router = useRouter()
  const route = useRoute()
  const { handleError } = useApiErrorHandler()
  const { cachedCall, generateKey, DEFAULT_TTL } = useApiCache()

  // Initialize state from URL query parameters
  const state = reactive<TenantListState>({
    tenants: [],
    loading: false,
    error: null,
    page: Number(route.query.page) || 1,
    totalPages: 0,
    totalElements: 0,
    pageSize: Number(route.query.size) || 20,
    search: (route.query.search as string) || '',
    status: (route.query.status as string) || '',
    sortBy: (route.query.sortBy as string) || 'createdAt',
    sortDirection: (route.query.sortDirection as 'asc' | 'desc') || 'desc'
  })

  // Sync state to URL query parameters
  const syncUrlParams = () => {
    const query: Record<string, string> = {}
    
    if (state.page > 1) query.page = String(state.page)
    if (state.pageSize !== 20) query.size = String(state.pageSize)
    if (state.search) query.search = state.search
    if (state.status) query.status = state.status
    if (state.sortBy !== 'createdAt') query.sortBy = state.sortBy
    if (state.sortDirection !== 'desc') query.sortDirection = state.sortDirection

    router.push({ query })
  }

  // Fetch tenants with current filters
  const fetchTenants = async (useCache = true) => {
    state.loading = true
    state.error = null

    try {
      const params = {
        page: state.page - 1, // Backend uses 0-based indexing
        size: state.pageSize,
        search: state.search || undefined,
        status: state.status || undefined,
        sortBy: state.sortBy,
        sortDirection: state.sortDirection
      }

      // Use cached call if enabled
      const response = useCache
        ? await cachedCall(
            () => api.getTenants(params),
            generateKey('/tenants', params),
            { ttl: DEFAULT_TTL.tenantList }
          )
        : await api.getTenants(params)

      state.tenants = response.content
      state.totalPages = response.totalPages
      state.totalElements = response.totalElements
    } catch (err: any) {
      state.error = err.message || 'Failed to fetch tenants'
      console.error('Error fetching tenants:', err)

      // Show user-friendly error notification
      handleError(err, 'Failed to load tenants. Please try again.')
    } finally {
      state.loading = false
    }
  }

  // Search with debouncing using utility
  const { debounce } = useDebounce()
  const debouncedFetch = debounce(() => {
    syncUrlParams()
    fetchTenants(false) // Don't use cache for search results
  }, 300)

  const setSearch = (value: string) => {
    state.search = value
    state.page = 1 // Reset to first page on search
    debouncedFetch()
  }

  // Set status filter
  const setStatus = (value: string) => {
    state.status = value
    state.page = 1 // Reset to first page on filter change
    syncUrlParams()
    fetchTenants()
  }

  // Set sort
  const setSort = (column: string) => {
    if (state.sortBy === column) {
      // Toggle direction if same column
      state.sortDirection = state.sortDirection === 'asc' ? 'desc' : 'asc'
    } else {
      state.sortBy = column
      state.sortDirection = 'asc'
    }
    syncUrlParams()
    fetchTenants()
  }

  // Set page
  const setPage = (page: number) => {
    state.page = page
    syncUrlParams()
    fetchTenants()
  }

  // Set page size
  const setPageSize = (size: number) => {
    state.pageSize = size
    state.page = 1 // Reset to first page
    syncUrlParams()
    fetchTenants()
  }

  // Navigate to tenant detail
  const viewTenant = (id: number) => {
    router.push(`/tenants/${id}`)
  }

  // Navigate to create tenant
  const createTenant = () => {
    router.push('/tenants/new')
  }

  // Refresh tenant list
  const refresh = () => {
    fetchTenants()
  }

  return {
    state,
    fetchTenants,
    setSearch,
    setStatus,
    setSort,
    setPage,
    setPageSize,
    viewTenant,
    createTenant,
    refresh
  }
}
