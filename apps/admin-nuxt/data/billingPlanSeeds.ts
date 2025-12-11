import type { TenantPlan } from '@/types/billing';

type SeedMap = Record<string, TenantPlan>;

const createPlan = (plan: Partial<TenantPlan>): TenantPlan => ({
  tenantId: plan.tenantId ?? 1,
  planTier: plan.planTier ?? 'PROFESSIONAL',
  planTierName: plan.planTierName ?? 'Professional Care',
  price: plan.price ?? 299,
  currency: plan.currency ?? 'USD',
  billingCycle: plan.billingCycle ?? 'MONTHLY',
  renewalDate: plan.renewalDate ?? new Date().toISOString(),
  paymentMethodMask: plan.paymentMethodMask ?? '•••• 4242',
  paymentMethodType: plan.paymentMethodType ?? 'VISA',
  status: plan.status ?? 'ACTIVE',
  cancellationDate: plan.cancellationDate ?? null,
  cancellationEffectiveDate: plan.cancellationEffectiveDate ?? null,
  pendingPlanTier: plan.pendingPlanTier ?? null,
  pendingPlanEffectiveDate: plan.pendingPlanEffectiveDate ?? null,
  features: plan.features ?? [
    'Unlimited staff seats',
    'Advanced analytics',
    'Priority support',
    'Custom branding'
  ],
  paypalSubscriptionId: plan.paypalSubscriptionId ?? 'I-DEFAULTSEED',
  maxStaff: plan.maxStaff ?? -1,
  maxDoctors: plan.maxDoctors ?? -1,
  staffUsed: plan.staffUsed ?? 0,
  doctorsUsed: plan.doctorsUsed ?? 0
});

export const billingPlanSeeds: SeedMap = {
  default: createPlan({
    tenantId: 1,
    planTier: 'PREMIUM',
    planTierName: 'Premium Care',
    price: 499,
    billingCycle: 'MONTHLY',
    renewalDate: '2025-12-01T09:00:00.000Z',
    paymentMethodMask: '•••• 9012',
    paymentMethodType: 'MASTERCARD',
    paypalSubscriptionId: 'I-DEFAULTPREMIUM'
  }),
  'medina-clinic': createPlan({
    tenantId: 2,
    planTier: 'GROWTH',
    planTierName: 'Growth Essentials',
    price: 199,
    currency: 'USD',
    billingCycle: 'MONTHLY',
    renewalDate: '2025-11-15T10:00:00.000Z',
    paymentMethodMask: '•••• 5521',
    paymentMethodType: 'VISA',
    paypalSubscriptionId: 'I-MEDINAGROWTH',
    features: [
      '10 staff seats',
      'Marketing automation',
      'Advanced reporting',
      'Email + chat support'
    ]
  }),
  'coastal-derma': createPlan({
    tenantId: 3,
    planTier: 'ENTERPRISE',
    planTierName: 'Enterprise Unlimited',
    price: 1299,
    billingCycle: 'ANNUAL',
    renewalDate: '2026-01-10T12:00:00.000Z',
    paymentMethodMask: '•••• 7788',
    paymentMethodType: 'AMEX',
    paypalSubscriptionId: 'I-COASTALENTERPRISE',
    features: [
      'Unlimited locations',
      'Dedicated success manager',
      'Custom integrations',
      '24/7 priority support'
    ]
  }),
  'aurora-dental': createPlan({
    tenantId: 4,
    planTier: 'STANDARD',
    planTierName: 'Standard Clinic',
    price: 129,
    billingCycle: 'MONTHLY',
    renewalDate: '2025-11-05T08:30:00.000Z',
    paymentMethodMask: '•••• 3344',
    paymentMethodType: 'VISA',
    paypalSubscriptionId: 'I-AURORASTANDARD',
    features: [
      '5 staff seats',
      'Patient reminders',
      'Basic analytics',
      'Email support'
    ]
  })
};
