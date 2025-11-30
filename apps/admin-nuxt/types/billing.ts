export interface BillingPlanResponse {
  tenantId: number
  planName: string
  planId: string
  status: string
  renewalDate: string | null
  amount: number | null
  currency: string | null
  billingCycle: string
  paymentMethodBrand: string | null
  paymentMethodLast4: string | null
  paymentMethodExpiry: string | null
  paypalSubscriptionId: string
  cancelPending: boolean
  includedFeatures: string[]
}

export interface CancelPlanPayload {
  reason?: string
}

// New types for subscription plan management
export interface TenantPlan {
  tenantId: number;
  planTier: string;
  planTierName: string;
  price: number;
  currency: string;
  billingCycle: string;
  renewalDate: string | null;
  paymentMethodMask: string | null;
  paymentMethodType: string | null;
  status: string;
  cancellationDate: string | null;
  cancellationEffectiveDate: string | null;
  pendingPlanTier: string | null;
  pendingPlanEffectiveDate: string | null;
  features: string[];
  paypalSubscriptionId: string;
}

export interface PlanChangeRequest {
  targetTier: string;
  billingCycle?: string;
  reason?: string;
}

export interface PlanChangeResponse {
  approvalUrl: string;
  newTier: string;
  effectiveDate: string;
  newPrice: number;
  currency: string;
  message: string;
}

export interface CancelPlanRequest {
  immediate?: boolean;
  reason?: string;
}

export interface CancellationResponse {
  effectiveDate: string;
  message: string;
  immediate: boolean;
}

export interface PaymentMethodUpdateResponse {
  portalUrl: string;
  message: string;
}
