import type { InsuranceCompany, InsuranceCompanyRequest } from '../types/insurance'

import { useAdminApi } from './useAdminApi'

export const useInsuranceCompanies = () => {
  const { request } = useAdminApi()

  const getInsuranceCompanies = async (): Promise<InsuranceCompany[]> => {
    try {
      const response = await request<InsuranceCompany[]>('/insurance-companies', {
        method: 'GET'
      })
      return response
    } catch (error) {
      console.error('Error fetching insurance companies:', error)
      throw error
    }
  }

  const getInsuranceCompany = async (id: number): Promise<InsuranceCompany> => {
    try {
      const response = await request<InsuranceCompany>(`/insurance-companies/${id}`, {
        method: 'GET'
      })
      return response
    } catch (error) {
      console.error('Error fetching insurance company:', error)
      throw error
    }
  }

  const createInsuranceCompany = async (data: InsuranceCompanyRequest): Promise<InsuranceCompany> => {
    try {
      const response = await request<InsuranceCompany>('/insurance-companies', {
        method: 'POST',
        body: data
      })
      return response
    } catch (error) {
      console.error('Error creating insurance company:', error)
      throw error
    }
  }

  const updateInsuranceCompany = async (id: number, data: InsuranceCompanyRequest): Promise<InsuranceCompany> => {
    try {
      const response = await request<InsuranceCompany>(`/insurance-companies/${id}`, {
        method: 'PUT',
        body: data
      })
      return response
    } catch (error) {
      console.error('Error updating insurance company:', error)
      throw error
    }
  }

  const deleteInsuranceCompany = async (id: number): Promise<void> => {
    try {
      await request<void>(`/insurance-companies/${id}`, {
        method: 'DELETE'
      })
    } catch (error) {
      console.error('Error deleting insurance company:', error)
      throw error
    }
  }

  return {
    getInsuranceCompanies,
    getInsuranceCompany,
    createInsuranceCompany,
    updateInsuranceCompany,
    deleteInsuranceCompany
  }
}