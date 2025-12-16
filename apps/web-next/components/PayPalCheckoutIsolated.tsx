'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import { useLocale } from 'next-intl';
import { api } from '@/lib/api';

interface PayPalCheckoutProps {
  // Legacy virtual consultation flow
  amount?: number;
  currency?: string;
  patientId?: number;
  doctorId?: number;
  serviceId?: number;
  slotId?: string;
  // Cart flow
  orderId?: number;
  returnUrl?: string;
  cancelUrl?: string;
  onSuccess: (orderID: string) => void;
  onError: (error: string) => void;
  onCancel: () => void;
  onReady?: () => void;
}

declare global {
  interface Window {
    paypal?: any;
    __PAYPAL_INSTANCE_COUNT__?: number;
  }
}

// Global state to track PayPal instances
let paypalInstanceCount = 0;
let paypalScriptPromise: Promise<void> | null = null;

export default function PayPalCheckoutIsolated({
  amount,
  currency,
  patientId,
  doctorId,
  serviceId,
  slotId,
  orderId,
  returnUrl,
  cancelUrl,
  onSuccess,
  onError,
  onCancel,
  onReady,
}: PayPalCheckoutProps) {
  const paypalRef = useRef<HTMLDivElement>(null);
  const instanceIdRef = useRef<number>(0);
  const [isLoading, setIsLoading] = useState(true);
  const [paypalClientId, setPaypalClientId] = useState<string>('');
  const [error, setError] = useState<string>('');
  const locale = useLocale();
  const isRTL = locale === 'ar';

  const safeAmount = Number.isFinite(amount as number) ? (amount as number) : 0;
  const safeCurrency = currency || 'USD';

  // Assign unique instance ID
  useEffect(() => {
    paypalInstanceCount++;
    instanceIdRef.current = paypalInstanceCount;
    window.__PAYPAL_INSTANCE_COUNT__ = paypalInstanceCount;
    console.log(`PayPal instance ${instanceIdRef.current} created. Total instances: ${paypalInstanceCount}`);

    return () => {
      paypalInstanceCount--;
      window.__PAYPAL_INSTANCE_COUNT__ = paypalInstanceCount;
      console.log(`PayPal instance ${instanceIdRef.current} destroyed. Remaining instances: ${paypalInstanceCount}`);
    };
  }, []);

  // Load PayPal settings
  useEffect(() => {
    const loadPayPalSettings = async () => {
      try {
        console.log(`Instance ${instanceIdRef.current}: Loading PayPal settings...`);
        const settings = await api.getPaymentSettings();
        console.log(`Instance ${instanceIdRef.current}: PayPal settings loaded:`, settings);
        setPaypalClientId(settings.paypalClientId);
      } catch (error) {
        console.error(`Instance ${instanceIdRef.current}: Failed to load PayPal settings:`, error);
        setError('Failed to load payment configuration');
        onError('Failed to load payment configuration');
      }
    };

    loadPayPalSettings();
  }, [onError]);

  // Load PayPal script (singleton)
  const loadPayPalScript = useCallback(async (clientId: string, currency: string, locale: string): Promise<void> => {
    if (window.paypal) {
      console.log(`Instance ${instanceIdRef.current}: PayPal already loaded`);
      return Promise.resolve();
    }

    if (paypalScriptPromise) {
      console.log(`Instance ${instanceIdRef.current}: PayPal script already loading, waiting...`);
      return paypalScriptPromise;
    }

    // Map locale to PayPal supported locale format
    const getPayPalLocale = (appLocale: string): string => {
      switch (appLocale) {
        case 'ar':
          return 'ar_EG'; // Arabic (Egypt) - most commonly supported Arabic locale by PayPal
        case 'en':
        default:
          return 'en_US';
      }
    };

    const paypalLocale = getPayPalLocale(locale);
    console.log(`Instance ${instanceIdRef.current}: Loading PayPal script with locale: ${paypalLocale}...`);
    
    paypalScriptPromise = new Promise((resolve, reject) => {
      // Remove any existing script
      const existingScript = document.querySelector('script[src*="paypal.com/sdk/js"]');
      if (existingScript) {
        existingScript.remove();
      }

      const script = document.createElement('script');
      script.src = `https://www.paypal.com/sdk/js?client-id=${clientId}&currency=${currency}&intent=capture&locale=${paypalLocale}`;
      script.async = true;
      
      script.onload = () => {
        console.log(`Instance ${instanceIdRef.current}: PayPal script loaded successfully with locale: ${paypalLocale}`);
        resolve();
      };
      
      script.onerror = () => {
        console.error(`Instance ${instanceIdRef.current}: Failed to load PayPal script`);
        paypalScriptPromise = null;
        reject(new Error('Failed to load PayPal SDK'));
      };

      document.head.appendChild(script);
    });

    return paypalScriptPromise;
  }, [locale]);

  // Render PayPal button
  const renderPayPalButton = useCallback(async () => {
    try {
      if (!paypalRef.current) {
        console.error(`Instance ${instanceIdRef.current}: PayPal ref not available`);
        return;
      }

      if (!window.paypal) {
        console.error(`Instance ${instanceIdRef.current}: PayPal SDK not loaded`);
        return;
      }

      // Check if this instance should render (only the first instance)
      if (instanceIdRef.current !== 1) {
        console.log(`Instance ${instanceIdRef.current}: Skipping render, not the primary instance`);
        setError('Multiple PayPal instances detected. Only one can be active at a time.');
        onError('Multiple PayPal instances detected. Please close other PayPal components.');
        return;
      }

      console.log(`Instance ${instanceIdRef.current}: Rendering PayPal button...`);

      // Clear container
      paypalRef.current.innerHTML = '';

      // Destroy existing components
      if (window.paypal.destroyAll) {
        try {
          window.paypal.destroyAll();
          console.log(`Instance ${instanceIdRef.current}: Destroyed existing PayPal components`);
        } catch (e) {
          console.warn(`Instance ${instanceIdRef.current}: Could not destroy existing components:`, e);
        }
      }

      // Remove any existing PayPal theme styles
      const existingStyle = document.getElementById('paypal-theme-styles-isolated');
      if (existingStyle) {
        existingStyle.remove();
      }

      // Use default PayPal button styling without theme customization
      const buttonStyle = {
        layout: 'vertical' as const,
        color: 'blue' as const,
        shape: 'rect' as const,
        label: 'pay' as const,
        height: 55,
        tagline: false,
      };

      console.log(`Instance ${instanceIdRef.current}: Using default button style:`, buttonStyle);

      const buttons = window.paypal.Buttons({
        style: buttonStyle,
        createOrder: async () => {
          try {
            // Cart flow: use existing orderId
            if (orderId) {
              const response = await api.initiateCartPayPalOrder(
                orderId,
                returnUrl || window.location.href,
                cancelUrl || window.location.href
              );
              const oid = response.providerOrderId || response.paymentId;
              if (!oid) {
                throw new Error(response.message || 'Failed to create PayPal order');
              }
              return oid;
            }

            // Virtual consultation flow
            const response = await api.createPayPalOrder({
              patientId: patientId as number,
              doctorId: doctorId as number,
              serviceId: serviceId as number,
              slotId: slotId as string,
            });
            return response.orderID;
          } catch (error: any) {
            console.error(`Instance ${instanceIdRef.current}: Error creating PayPal order:`, error);
            onError(error.message || 'Failed to create payment order');
            throw error;
          }
        },
        onApprove: async (data: any) => {
          try {
            console.log(`Instance ${instanceIdRef.current}: PayPal onApprove called with data:`, data);
            if (orderId) {
              const capture = await api.captureCartPayPalPayment(data.orderID);
              if (!capture?.success) {
                throw new Error(capture?.message || 'Payment capture failed');
              }
            } else {
              const response = await api.capturePayPalOrder(data.orderID);
              console.log(`Instance ${instanceIdRef.current}: Capture response:`, response);
              if (!response.success) {
                throw new Error(response.message || 'Payment capture failed');
              }
            }
            onSuccess(data.orderID);
          } catch (error: any) {
            console.error(`Instance ${instanceIdRef.current}: Error capturing PayPal order:`, error);
            onError(error.message || 'Payment processing failed');
          }
        },
        onCancel: () => {
          console.log(`Instance ${instanceIdRef.current}: Payment cancelled`);
          onCancel();
        },
        onError: (err: any) => {
          console.error(`Instance ${instanceIdRef.current}: PayPal SDK error:`, err);
          onError(`Payment processing error: ${err?.message || 'Unknown PayPal error'}`);
        },
      });

      await buttons.render(paypalRef.current);
      console.log(`Instance ${instanceIdRef.current}: PayPal button rendered successfully`);
      setIsLoading(false);
      setError('');
      onReady?.();

    } catch (error: any) {
      console.error(`Instance ${instanceIdRef.current}: Error rendering PayPal button:`, error);
      
      if (error.message && error.message.includes('zoid')) {
        setError('PayPal component conflict detected. Please refresh the page.');
        onError('PayPal component conflict detected. Please refresh the page.');
      } else {
        setError('Failed to initialize payment system');
        onError('Failed to initialize payment system');
      }
      onReady?.();
    }
  }, [patientId, doctorId, serviceId, slotId, onSuccess, onError, onCancel, onReady]);

  // Initialize PayPal
  useEffect(() => {
    if (!paypalClientId) return;

    const initializePayPal = async () => {
      try {
        await loadPayPalScript(paypalClientId, safeCurrency, locale);
        await renderPayPalButton();
      } catch (error) {
        console.error(`Instance ${instanceIdRef.current}: Failed to initialize PayPal:`, error);
        setError('Failed to initialize PayPal');
        onError('Failed to initialize PayPal');
        onReady?.();
      }
    };

    initializePayPal();
  }, [paypalClientId, safeCurrency, locale, loadPayPalScript, renderPayPalButton]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (paypalRef.current) {
        paypalRef.current.innerHTML = '';
      }
      
      // Only destroy all if this is the last instance
      if (paypalInstanceCount === 1 && window.paypal && window.paypal.destroyAll) {
        try {
          window.paypal.destroyAll();
          console.log(`Instance ${instanceIdRef.current}: Destroyed all PayPal components (last instance)`);
        } catch (e) {
          console.warn(`Instance ${instanceIdRef.current}: Could not destroy PayPal components:`, e);
        }
      }
    };
  }, []);

  if (error) {
    return (
      <div className="p-6 border border-red-200 rounded-lg bg-red-50 dark:bg-red-900/20 dark:border-red-800">
        <div className={`flex items-center gap-2 text-red-600 dark:text-red-400 mb-2 ${isRTL ? 'flex-row-reverse' : ''}`}>
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
          <span className="font-semibold">
            {locale === 'ar' ? 'خطأ في نظام الدفع' : 'Payment System Error'}
          </span>
        </div>
        <p className="text-sm text-red-600 dark:text-red-400 mb-4">{error}</p>
        <button
          onClick={() => window.location.reload()}
          className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
        >
          {locale === 'ar' ? 'تحديث الصفحة' : 'Refresh Page'}
        </button>
      </div>
    );
  }

  return (
    <div className={`relative z-20 w-full space-y-6 ${isRTL ? 'rtl' : 'ltr'}`} dir={isRTL ? 'rtl' : 'ltr'}>
      {/* Instance Info (Development) */}
      {process.env.NODE_ENV === 'development' && (
        <div className="text-xs text-gray-500 bg-gray-100 dark:bg-gray-800 p-2 rounded">
          Instance #{instanceIdRef.current} | Total: {window.__PAYPAL_INSTANCE_COUNT__ || 0} | Locale: {locale} ({isRTL ? 'RTL' : 'LTR'})
        </div>
      )}

      {/* Payment Summary Card */}
      <div className="relative overflow-hidden rounded-2xl border border-violet-100 dark:border-violet-800/30 bg-gradient-to-br from-violet-50 via-white to-cyan-50 dark:from-violet-950/50 dark:via-slate-900 dark:to-cyan-950/50 p-6 shadow-lg dark:shadow-violet-900/20">
        <div className="absolute inset-0 bg-gradient-to-br from-violet-500/5 to-cyan-500/5 dark:from-violet-400/10 dark:to-cyan-400/10"></div>
        <div className="relative">
          <div className={`flex items-center justify-between ${isRTL ? 'flex-row-reverse' : ''}`}>
            <div className="space-y-1">
              <div className={`flex items-center gap-2 ${isRTL ? 'flex-row-reverse' : ''}`}>
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-r from-violet-500 to-cyan-500 dark:from-violet-400 dark:to-cyan-400">
                  <svg className="h-4 w-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <h4 className="text-lg font-semibold text-slate-900 dark:text-slate-100">
                  {orderId ? (locale === 'ar' ? 'إجمالي الطلب' : 'Order Total') : locale === 'ar' ? 'دفع آمن' : 'Secure Payment'}
                </h4>
              </div>
              <p className="text-sm text-slate-600 dark:text-slate-400">
                {orderId
                  ? locale === 'ar'
                    ? 'سيتم دفع هذا المبلغ عبر PayPal'
                    : 'This amount will be charged via PayPal'
                  : locale === 'ar'
                  ? 'رسوم الاستشارة الافتراضية'
                  : 'Virtual consultation fee'}
              </p>
            </div>
            <div className={`${isRTL ? 'text-left' : 'text-right'}`}>
              <div className="text-2xl font-bold bg-gradient-to-r from-violet-600 to-cyan-600 dark:from-violet-400 dark:to-cyan-400 bg-clip-text text-transparent">
                ${safeAmount.toFixed(2)}
              </div>
              <div className="text-xs text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                {safeCurrency}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Payment Method Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          <div className="h-px flex-1 bg-gradient-to-r from-transparent via-slate-200 dark:via-slate-700 to-transparent"></div>
          <span className="text-sm font-medium text-slate-500 dark:text-slate-400">
            {locale === 'ar' ? 'اختر طريقة الدفع' : 'Choose Payment Method'}
          </span>
          <div className="h-px flex-1 bg-gradient-to-r from-transparent via-slate-200 dark:via-slate-700 to-transparent"></div>
        </div>

        {isLoading && (
          <div className="flex items-center justify-center py-12">
            <div className="relative">
              <div className="animate-spin rounded-full h-12 w-12 border-4 border-violet-100 dark:border-violet-800/50"></div>
              <div className="animate-spin rounded-full h-12 w-12 border-4 border-t-violet-500 dark:border-t-violet-400 absolute inset-0"></div>
            </div>
            <div className={`${isRTL ? 'mr-4' : 'ml-4'} space-y-1`}>
              <div className="text-slate-600 dark:text-slate-300 font-medium">
                {locale === 'ar' ? 'جاري تحميل خيارات الدفع الآمنة...' : 'Loading secure payment options...'}
              </div>
              <div className="text-xs text-slate-500 dark:text-slate-400">
                {locale === 'ar' ? 'تهيئة دفع PayPal' : 'Initializing PayPal checkout'} ({locale.toUpperCase()})
              </div>
            </div>
          </div>
        )}

        {/* PayPal Button Container */}
        <div className={`${isLoading ? 'hidden' : 'block'} space-y-4`}>
          <div className="relative rounded-xl border border-slate-200 dark:border-slate-700/50 bg-white dark:bg-slate-800 p-6 shadow-lg">
            <div className="mb-4">
              <h3 className="text-sm font-semibold text-slate-700 dark:text-slate-200">
                {locale === 'ar' ? 'دفع آمن مع PayPal' : 'Secure Payment with PayPal'}
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                {locale === 'ar' ? 'نسخة معزولة - تمنع التعارضات' : 'Isolated instance - prevents conflicts'}
              </p>
            </div>
            
            <div ref={paypalRef} className="paypal-button-container"></div>
          </div>
        </div>
      </div>

      {/* Security Notice */}
      <div className="rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 p-4 text-center">
        <div className="flex items-center justify-center gap-2 text-sm text-slate-600 dark:text-slate-400">
          <svg className="h-4 w-4 text-green-500 dark:text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span className="font-medium">
            {locale === 'ar' 
              ? 'تشفير SSL 256-بت • متوافق مع PCI DSS • بياناتك آمنة'
              : '256-bit SSL encryption • PCI DSS compliant • Your data is secure'
            }
          </span>
        </div>
      </div>
    </div>
  );
}
