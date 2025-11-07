import type { FetchOptions } from 'ofetch'

export type TreatmentPlanStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export type PaymentMethod = 'CASH' | 'PAYPAL' | 'POS' | 'BANK_TRANSFER' | 'OTHER'
export type FollowUpCadence = 'WEEKLY' | 'MONTHLY'

export interface ScheduledFollowUp {
  appointmentId: number
  visitNumber: number
  scheduledAt: string
  status: string
  bookingMode: string
  paymentCollected: boolean
  patientAttended: boolean | null
}

export interface TreatmentPlan {
  id: number
  patientId: number
  patientName: string
  doctorId: number
  doctorName: string
  treatmentTypeId: number
  treatmentTypeName: string
  totalPrice: number
  currency: string
  plannedFollowups: number
  followUpCadence: FollowUpCadence
  completedVisits: number
  status: TreatmentPlanStatus
  notes?: string
  discountAmount?: number
  discountReason?: string
  createdAt: string
  updatedAt: string
  startedAt?: string
  completedAt?: string
  // Dynamic calculations (original currency)
  totalPaid: number
  remainingBalance: number
  remainingVisits: number
  expectedPaymentPerVisit: number
  totalMaterialsCost: number
  netRevenue: number
  // Converted prices (in clinic currency)
  convertedTotalPrice: number
  convertedCurrency: string
  convertedTotalPaid: number
  convertedRemainingBalance: number
  convertedExpectedPaymentPerVisit: number
  convertedTotalMaterialsCost: number
  convertedNetRevenue: number
  followUpVisits: FollowUpVisit[]
  scheduledFollowUps: ScheduledFollowUp[]
}

export interface FollowUpVisit {
  id: number
  visitNumber: number
  appointmentId?: number | null
  visitDate: string
  notes?: string
  performedProcedures?: string
  createdAt: string
  payments: Payment[]
  materials: MaterialUsage[]
  totalPaymentsThisVisit: number
  totalMaterialsCostThisVisit: number
}

export interface Payment {
  id: number
  amount: number
  currency: string
  convertedAmount: number
  convertedCurrency: string
  paymentMethod: PaymentMethod
  transactionReference?: string
  paymentDate: string
  notes?: string
  createdAt: string
}

export interface MaterialUsage {
  id: number
  materialId: number
  materialName: string
  quantity: number
  unitCost: number
  currency: string
  totalCost: number
  convertedTotalCost: number
  convertedCurrency: string
  notes?: string
  createdAt: string
}

export interface CreateTreatmentPlanRequest {
  patientId: number
  doctorId: number
  treatmentTypeId: number
  totalPrice: number
  currency: string
  plannedFollowups: number
  followUpCadence: FollowUpCadence
  notes?: string
}

export interface UpdateTreatmentPlanRequest {
  totalPrice: number
  plannedFollowups: number
  followUpCadence: FollowUpCadence
  notes?: string
}

export interface ApplyDiscountRequest {
  discountAmount: number
  discountReason: string
}

export interface RecordVisitRequest {
  appointmentId?: number
  visitDate: string
  notes?: string
  performedProcedures?: string
  payments?: {
    amount: number
    paymentMethod: PaymentMethod
    transactionReference?: string
    paymentDate: string
    notes?: string
  }[]
  materials?: {
    materialId: number
    quantity: number
    notes?: string
  }[]
}

export function useTreatmentPlans() {
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
    // Get all treatment plans
    async getAll(filters?: {
      patientId?: number
      doctorId?: number
      status?: TreatmentPlanStatus
    }): Promise<TreatmentPlan[]> {
      const params = new URLSearchParams()
      if (filters?.patientId) params.append('patientId', filters.patientId.toString())
      if (filters?.doctorId) params.append('doctorId', filters.doctorId.toString())
      if (filters?.status) params.append('status', filters.status)

      const query = params.toString() ? `?${params.toString()}` : ''
      return authorizedRequest<TreatmentPlan[]>(`/treatment-plans${query}`)
    },

    // Get treatment plan by ID
    async getById(id: number): Promise<TreatmentPlan> {
      return authorizedRequest<TreatmentPlan>(`/treatment-plans/${id}`)
    },

    // Create new treatment plan
    async create(data: CreateTreatmentPlanRequest): Promise<TreatmentPlan> {
      return authorizedRequest<TreatmentPlan>('/treatment-plans', {
        method: 'POST',
        body: data
      })
    },

    // Update treatment plan
    async update(id: number, data: UpdateTreatmentPlanRequest): Promise<TreatmentPlan> {
      return authorizedRequest<TreatmentPlan>(`/treatment-plans/${id}`, {
        method: 'PUT',
        body: data
      })
    },

    // Apply discount
    async applyDiscount(id: number, data: ApplyDiscountRequest): Promise<TreatmentPlan> {
      return authorizedRequest<TreatmentPlan>(`/treatment-plans/${id}/discount`, {
        method: 'POST',
        body: data
      })
    },

    // Record follow-up visit
    async recordVisit(id: number, data: RecordVisitRequest): Promise<TreatmentPlan> {
      return authorizedRequest<TreatmentPlan>(`/treatment-plans/${id}/visits`, {
        method: 'POST',
        body: data
      })
    },

    // Update recorded follow-up visit
    async updateVisit(planId: number, visitId: number, data: RecordVisitRequest): Promise<TreatmentPlan> {
      return authorizedRequest<TreatmentPlan>(`/treatment-plans/${planId}/visits/${visitId}`, {
        method: 'PUT',
        body: data
      })
    },

    // Complete treatment plan
    async complete(id: number): Promise<TreatmentPlan> {
      return authorizedRequest<TreatmentPlan>(`/treatment-plans/${id}/complete`, {
        method: 'POST'
      })
    },

    // Cancel treatment plan
    async cancel(id: number, reason?: string): Promise<TreatmentPlan> {
      return authorizedRequest<TreatmentPlan>(`/treatment-plans/${id}/cancel`, {
        method: 'POST',
        body: { reason }
      })
    }
  }
}
