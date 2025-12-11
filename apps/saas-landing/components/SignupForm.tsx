'use client';

import { useState, useCallback, useEffect, useRef } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Loader2, Check, X, AlertCircle } from 'lucide-react';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { API_BASE_URL, PAYPAL_CLIENT_ID } from '@/lib/constants';
import { trackFormSubmission } from '@/lib/analytics';

// Validation schema
const signupSchema = z.object({
  clinicName: z.string()
    .min(2, 'Clinic name must be at least 2 characters')
    .max(100, 'Clinic name must be less than 100 characters'),
  subdomain: z.string()
    .min(3, 'Subdomain must be at least 3 characters')
    .max(63, 'Subdomain must be less than 63 characters')
    .regex(/^[a-z0-9]([a-z0-9-]*[a-z0-9])?$/, 'Subdomain must contain only lowercase letters, numbers, and hyphens (cannot start or end with hyphen)'),
  ownerName: z.string()
    .min(2, 'Owner name must be at least 2 characters')
    .max(100, 'Owner name must be less than 100 characters'),
  email: z.string()
    .email('Please enter a valid email address'),
  password: z.string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
    .regex(/[0-9]/, 'Password must contain at least one number')
    .regex(/[^A-Za-z0-9]/, 'Password must contain at least one special character'),
  phone: z.string()
    .min(10, 'Phone number must be at least 10 characters')
    .max(20, 'Phone number must be less than 20 characters')
    .regex(/^\+?[0-9\s\-()]+$/, 'Please enter a valid phone number'),
  planTier: z.string().min(1, 'Please select a plan'),
  billingCycle: z.enum(['MONTHLY', 'ANNUAL'], {
    required_error: 'Please select a billing cycle',
  }),
});

type SignupFormValues = z.infer<typeof signupSchema>;

type SubdomainCheckStatus = 'idle' | 'checking' | 'available' | 'unavailable' | 'error';

type PlanOption = {
  value: string;
  label: string;
  price: string;
  description: string;
};

const DEFAULT_PLAN_OPTIONS: readonly PlanOption[] = [
  {
    value: 'BASIC',
    label: 'Basic',
    price: '$29/mo',
    description: 'Essential tools for solo clinics',
  },
  {
    value: 'PROFESSIONAL',
    label: 'Professional',
    price: '$79/mo',
    description: 'Automation + growth insights',
  },
  {
    value: 'ENTERPRISE',
    label: 'Enterprise',
    price: 'Custom',
    description: 'Multi-location & custom workflows',
  },
] as const;

const billingOptions = [
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'ANNUAL', label: 'Annual (2 months free)' },
] as const;

const formatPrice = (value: number | string | null | undefined, currency: string) => {
  if (value === null || value === undefined) return null;
  if (typeof value === 'string' && !Number.isFinite(Number(value))) return value;
  const numeric = Number(value);
  if (!Number.isFinite(numeric)) return null;
  try {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
      maximumFractionDigits: 0,
    }).format(numeric);
  } catch {
    return `${currency || 'USD'} ${numeric}`;
  }
};

export function SignupForm() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [subdomainStatus, setSubdomainStatus] = useState<SubdomainCheckStatus>('idle');
  const [subdomainCheckTimeout, setSubdomainCheckTimeout] = useState<NodeJS.Timeout | null>(null);
  const [planOptions, setPlanOptions] = useState<PlanOption[]>([...DEFAULT_PLAN_OPTIONS]);
  const [loadingPlans, setLoadingPlans] = useState(false);
  const [paypalReady, setPaypalReady] = useState(false);
  const [paypalError, setPaypalError] = useState<string | null>(null);
  const paypalButtonsRendered = useRef(false);
  const paypalContainerRef = useRef<HTMLDivElement | null>(null);

  const form = useForm<SignupFormValues>({
    resolver: zodResolver(signupSchema),
    defaultValues: {
      clinicName: '',
      subdomain: '',
      ownerName: '',
      email: '',
      password: '',
      phone: '',
      planTier: DEFAULT_PLAN_OPTIONS[0].value,
      billingCycle: 'MONTHLY',
    },
  });



  // Debounced subdomain availability check
  const checkSubdomainAvailability = useCallback(async (subdomain: string) => {
    if (!subdomain || subdomain.length < 3) {
      setSubdomainStatus('idle');
      return;
    }

    // Validate subdomain format first
    const subdomainRegex = /^[a-z0-9]([a-z0-9-]*[a-z0-9])?$/;
    if (!subdomainRegex.test(subdomain)) {
      setSubdomainStatus('idle');
      return;
    }

    setSubdomainStatus('checking');

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/public/signup/check-subdomain?subdomain=${encodeURIComponent(subdomain)}`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );

      if (response.ok) {
        const data = await response.json();
        setSubdomainStatus(data.available ? 'available' : 'unavailable');
        
        if (!data.available) {
          form.setError('subdomain', {
            type: 'manual',
            message: 'This subdomain is already taken',
          });
        }
      } else {
        setSubdomainStatus('error');
      }
    } catch (error) {
      console.error('Error checking subdomain:', error);
      setSubdomainStatus('error');
    }
  }, [form]);

  // Watch subdomain field and trigger debounced check
  useEffect(() => {
    const fetchPlans = async () => {
      setLoadingPlans(true);
      try {
        const res = await fetch(`${API_BASE_URL}/api/public/plans?currency=USD`, { cache: 'no-store' });
        if (!res.ok) throw new Error(`Failed to fetch plans ${res.status}`);
        const json = await res.json();
        const plans = Array.isArray(json) ? json : Array.isArray(json?.data) ? json.data : [];

        const formatted: PlanOption[] = plans.map((plan: any) => {
          const price = formatPrice(plan?.monthlyPrice, plan?.currency || 'USD');
          return {
            value: plan?.tier || 'BASIC',
            label: plan?.name || plan?.tier || 'Basic',
            price: price ?? 'Custom',
            description: plan?.description || 'Choose the plan that fits your clinic.',
          };
        });

        if (formatted.length) {
          setPlanOptions(formatted);
          form.setValue('planTier', formatted[0].value);
        }
      } catch (error) {
        console.error('Unable to load plans for signup form', error);
      } finally {
        setLoadingPlans(false);
      }
    };

    fetchPlans();
  }, [form]);

  // Load PayPal JS SDK for inline subscription
  useEffect(() => {
    const loadPayPalSdk = async () => {
      if (!PAYPAL_CLIENT_ID) {
        setPaypalError('PayPal client ID is not configured.');
        return;
      }

      if (typeof window === 'undefined') return;
      if ((window as any).paypal) {
        setPaypalReady(true);
        return;
      }

      return new Promise<void>((resolve, reject) => {
        const script = document.createElement('script');
        script.src = `https://www.paypal.com/sdk/js?client-id=${PAYPAL_CLIENT_ID}&intent=subscription&vault=true&components=buttons`;
        script.async = true;
        script.onload = () => {
          setPaypalReady(true);
          resolve();
        };
        script.onerror = () => {
          setPaypalError('Unable to load PayPal. Please refresh and try again.');
          reject(new Error('Failed to load PayPal SDK'));
        };
        document.body.appendChild(script);
      });
    };

    loadPayPalSdk();
  }, []);

  useEffect(() => {
    const subscription = form.watch((value, { name }) => {
      if (name === 'subdomain') {
        const subdomain = value.subdomain;
        
        // Clear previous timeout
        if (subdomainCheckTimeout) {
          clearTimeout(subdomainCheckTimeout);
        }

        // Set new timeout for debounced check
        if (subdomain && subdomain.length >= 3) {
          const timeout = setTimeout(() => {
            checkSubdomainAvailability(subdomain);
          }, 500); // 500ms debounce
          
          setSubdomainCheckTimeout(timeout);
        } else {
          setSubdomainStatus('idle');
        }
      }
    });

    return () => {
      subscription.unsubscribe();
      if (subdomainCheckTimeout) {
        clearTimeout(subdomainCheckTimeout);
      }
    };
  }, [form, checkSubdomainAvailability, subdomainCheckTimeout]);

  // Inline PayPal Buttons rendering
  useEffect(() => {
    if (!paypalReady) return;
    if (paypalButtonsRendered.current) return;
    if (!paypalContainerRef.current) return;

    const paypal = (window as any).paypal;
    if (!paypal) {
      setPaypalError('PayPal SDK not available. Please refresh and try again.');
      return;
    }

    paypal.Buttons({
      style: {
        color: 'gold',
        shape: 'pill',
        label: 'subscribe',
        layout: 'vertical',
      },
      createSubscription: async () => {
        setIsSubmitting(true);
        setSubmitError(null);
        const valid = await form.trigger();
        if (!valid) {
          setIsSubmitting(false);
          throw new Error('Please fix the highlighted fields.');
        }

        const values = form.getValues();
        const selectedPlan = planOptions.find((p) => p.value === values.planTier);
        if (!selectedPlan) {
          form.setError('planTier', { type: 'manual', message: 'Please select a valid plan' });
          setIsSubmitting(false);
          throw new Error('Plan not selected');
        }

        const response = await fetch(`${API_BASE_URL}/api/public/signup/inline`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            clinicName: values.clinicName,
            subdomain: values.subdomain,
            ownerName: values.ownerName,
            email: values.email,
            password: values.password,
            phone: values.phone,
            planTier: values.planTier,
            billingCycle: values.billingCycle,
            clientBaseUrl: window.location.origin,
          }),
        });

        const data = await response.json();
        if (!response.ok || !data.success || !data.subscriptionId) {
          const errorMessage = data.error || 'Unable to start subscription. Please try again.';
          setSubmitError(errorMessage);
          setIsSubmitting(false);
          throw new Error(errorMessage);
        }

        // Track successful form submission start
        trackFormSubmission('signup_form', 'signup_inline_start');

        return data.subscriptionId;
      },
      onApprove: async (data: any) => {
        try {
          const confirmUrl = `${API_BASE_URL}/api/public/payment-confirmation?subscription_id=${encodeURIComponent(
            data.subscriptionID
          )}`;
          const res = await fetch(confirmUrl, { method: 'GET' });
          const json = await res.json();

          if (res.ok && json.success && json.redirectUrl) {
            trackFormSubmission('signup_form', 'signup_inline_success');
            window.location.href = json.redirectUrl;
          } else {
            const msg = json.error || 'Payment verification failed. Please contact support.';
            setSubmitError(msg);
          }
        } catch (err) {
          console.error('Payment confirmation error:', err);
          setSubmitError('Unable to verify payment. Please try again.');
        } finally {
          setIsSubmitting(false);
        }
      },
      onError: (err: any) => {
        console.error('PayPal error:', err);
        setSubmitError('PayPal could not process the payment. Please try again.');
        setIsSubmitting(false);
      },
      onCancel: () => {
        setIsSubmitting(false);
        setSubmitError('Payment was cancelled.');
      },
    }).render(paypalContainerRef.current);

    paypalButtonsRendered.current = true;
  }, [paypalReady, form, planOptions]);

  const getSubdomainIndicator = () => {
    switch (subdomainStatus) {
      case 'checking':
        return <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />;
      case 'available':
        return <Check className="h-4 w-4 text-green-600" />;
      case 'unavailable':
        return <X className="h-4 w-4 text-destructive" />;
      case 'error':
        return <AlertCircle className="h-4 w-4 text-yellow-600" />;
      default:
        return null;
    }
  };

  return (
    <div className="w-full max-w-2xl mx-auto p-6 bg-background rounded-lg border border-border shadow-lg">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-foreground">Start Your Free Trial</h2>
        <p className="text-sm text-muted-foreground mt-2">
          Create your clinic portal in minutes. Pay securely with PayPal without leaving this page.
        </p>
      </div>

      {submitError && (
        <div className="mb-6 p-4 bg-destructive/10 border border-destructive/20 rounded-md">
          <div className="flex items-start gap-3">
            <AlertCircle className="h-5 w-5 text-destructive flex-shrink-0 mt-0.5" />
            <div>
              <h3 className="text-sm font-semibold text-destructive">Signup Failed</h3>
              <p className="text-sm text-destructive/90 mt-1">{submitError}</p>
            </div>
          </div>
        </div>
      )}

      <Form {...form}>
        <form onSubmit={(e) => e.preventDefault()} className="space-y-6">
          <FormField
            control={form.control}
            name="planTier"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Choose Your Plan</FormLabel>
                <FormDescription>
                  Select the toolkit that matches your growth stage.
                  {loadingPlans && <span className="ml-2 text-xs text-muted-foreground">(Updating from billing...)</span>}
                </FormDescription>
                <div className="grid gap-3 sm:grid-cols-3">
                  {planOptions.map((plan) => {
                    const isSelected = field.value === plan.value;
                    return (
                      <button
                        type="button"
                        key={plan.value}
                        onClick={() => field.onChange(plan.value)}
                        className={`rounded-lg border p-4 text-left transition hover:border-primary ${
                          isSelected ? 'border-primary bg-primary/5 shadow' : 'border-border bg-background'
                        }`}
                      >
                        <div className="flex items-baseline gap-1">
                          <span className="text-lg font-semibold text-foreground">{plan.label}</span>
                          <span className="text-sm text-muted-foreground">{plan.price}</span>
                        </div>
                        <p className="mt-2 text-sm text-muted-foreground">{plan.description}</p>
                      </button>
                    );
                  })}
                </div>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="billingCycle"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Billing Cycle</FormLabel>
                <FormDescription>Monthly keeps things flexible, annual locks in savings.</FormDescription>
                <div className="flex flex-wrap gap-3">
                  {billingOptions.map((option) => {
                    const isSelected = field.value === option.value;
                    return (
                      <button
                        type="button"
                        key={option.value}
                        onClick={() => field.onChange(option.value)}
                        className={`rounded-full border px-4 py-2 text-sm transition ${
                          isSelected ? 'border-primary bg-primary/10 text-primary' : 'border-border text-muted-foreground'
                        }`}
                      >
                        {option.label}
                      </button>
                    );
                  })}
                </div>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="clinicName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Clinic Name *</FormLabel>
                <FormControl>
                  <Input
                    placeholder="Happy Dental Clinic"
                    autoComplete="organization"
                    {...field}
                    disabled={isSubmitting}
                  />
                </FormControl>
                <FormDescription>
                  The official name of your clinic
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="subdomain"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Subdomain *</FormLabel>
                <FormControl>
                  <div className="relative">
                    <Input
                      placeholder="happy-dental"
                      {...field}
                      disabled={isSubmitting}
                      onChange={(e) => {
                        const value = e.target.value.toLowerCase();
                        field.onChange(value);
                      }}
                    />
                    <div className="absolute right-3 top-1/2 -translate-y-1/2">
                      {getSubdomainIndicator()}
                    </div>
                  </div>
                </FormControl>
                <FormDescription>
                  Your clinic URL will be: <span className="font-mono text-xs">{field.value || 'your-subdomain'}.clinic.com</span>
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="ownerName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Owner Name *</FormLabel>
                <FormControl>
                  <Input
                    placeholder="Dr. John Smith"
                    autoComplete="name"
                    {...field}
                    disabled={isSubmitting}
                  />
                </FormControl>
                <FormDescription>
                  Full name of the clinic owner/administrator
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="email"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Email Address *</FormLabel>
                <FormControl>
                  <Input
                    type="email"
                    placeholder="john@happydental.com"
                    autoComplete="email"
                    {...field}
                    disabled={isSubmitting}
                  />
                </FormControl>
                <FormDescription>
                  We&apos;ll use this for your account login
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="password"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Password *</FormLabel>
                <FormControl>
                  <Input
                    type="password"
                    placeholder="••••••••"
                    autoComplete="new-password"
                    {...field}
                    disabled={isSubmitting}
                  />
                </FormControl>
                <FormDescription>
                  Must be at least 8 characters with uppercase, lowercase, number, and special character
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="phone"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Phone Number *</FormLabel>
                <FormControl>
                  <Input
                    type="tel"
                    placeholder="+1234567890"
                    autoComplete="tel"
                    {...field}
                    disabled={isSubmitting}
                  />
                </FormControl>
                <FormDescription>
                  Include country code (e.g., +1 for US)
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <div className="pt-4 space-y-3">
            {paypalError && (
              <div className="p-3 rounded-md bg-destructive/10 border border-destructive/30 text-destructive text-sm">
                {paypalError}
              </div>
            )}
            <div ref={paypalContainerRef} className="flex justify-center" />
            <p className="text-xs text-center text-muted-foreground">
              By signing up, you agree to our Terms of Service and Privacy Policy.
              Your payment is securely processed by PayPal.
            </p>
          </div>
        </form>
      </Form>
    </div>
  );
}
