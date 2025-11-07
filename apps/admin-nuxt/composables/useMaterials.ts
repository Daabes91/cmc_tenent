import type { FetchOptions } from 'ofetch'

export interface Material {
  id: number
  name: string
  description?: string
  unitCost: number
  currency: string
  convertedPrice: number
  convertedCurrency: string
  unitOfMeasure?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateMaterialRequest {
  name: string
  description?: string
  unitCost: number
  currency?: string
  unitOfMeasure?: string
  active: boolean
}

export interface UpdateMaterialRequest {
  name: string
  description?: string
  unitCost: number
  currency?: string
  unitOfMeasure?: string
  active: boolean
}

export function useMaterials() {
  const config = useRuntimeConfig()
  const baseURL = useApiBase()
  const auth = useAuth()

  const authorizedRequest = async <T>(
    path: string,
    options: FetchOptions = {}
  ): Promise<T> => {
    return await $fetch<T>(path, {
      baseURL,
      credentials: 'include',
      headers: {
        ...auth.authorizationHeader(),
        ...(options.headers ?? {})
      },
      ...options
    })
  }

  return {
    // Get all materials
    async getAll(activeOnly = false): Promise<Material[]> {
      const query = activeOnly ? '?activeOnly=true' : ''
      return authorizedRequest<Material[]>(`/materials${query}`)
    },

    // Get material by ID
    async getById(id: number): Promise<Material> {
      return authorizedRequest<Material>(`/materials/${id}`)
    },

    // Create new material
    async create(data: CreateMaterialRequest): Promise<Material> {
      return authorizedRequest<Material>('/materials', {
        method: 'POST',
        body: data
      })
    },

    // Update material
    async update(id: number, data: UpdateMaterialRequest): Promise<Material> {
      return authorizedRequest<Material>(`/materials/${id}`, {
        method: 'PUT',
        body: data
      })
    },

    // Activate material
    async activate(id: number): Promise<Material> {
      return authorizedRequest<Material>(`/materials/${id}/activate`, {
        method: 'POST'
      })
    },

    // Deactivate material
    async deactivate(id: number): Promise<Material> {
      return authorizedRequest<Material>(`/materials/${id}/deactivate`, {
        method: 'POST'
      })
    }
  }
}
