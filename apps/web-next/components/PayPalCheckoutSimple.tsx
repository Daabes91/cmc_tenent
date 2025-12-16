'use client';

import { useEffect, useRef, useState } from 'react';
import { api } from '@/lib/api';
import { loadPayPalScript, cleanupPayPalComponents, isPayPalReady } from '@/utils/paypalUtils';

interface PayPalCheckoutProps {
  amount: number;
  currency: string;
  patientId: number;
  doctorId: number;
  serviceId: number;
  slotId: string;
  onSuccess: (orderID: string) => void;
  onError: (error: string) => void;
  onCancel: () => void;
}

declare global {
  interface Window {
    paypal?: any;
  }
}

export default function PayPalCheckoutSimple({
  amount,
  currency,
  patientId,
  doctorId,
  serviceId,
  slotId,
  onSuccess,
  onError,
  onCancel,
}: PayPalCheckoutProps) {
  const paypalRef = useRef<HTMLDivElement>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [paypalClientId, setPaypalClientId] = useState<string>('');
  const [isRendered, setIsRendered] = useState(false);

  useEffect(() => {
    const loadPayPalSettings = async () => {
      try {
        console.log('Loading PayPal settings...');
        const settings = await api.getPaymentSettings();
        console.log('PayPal settings loaded:', settings);
        setPaypalClientId(settings.paypalClientId);
      } catch (error) {
        console.error('Failed to load PayPal settings:', error);
        onError('Failed to load payment configuration');
      }
    };

    loadPayPalSettings();
  }, [onError]);

  useEffect(() => {
    if (!paypalClientId || isRendered) return;

    const initializePayPal = async () => {
      try {
        if (isPayPalReady()) {
          renderPayPalButton();
          return;
        }

        console.log('Loading PayPal script...');
        await loadPayPalScript({
          clientId: paypalClientId,
          currency: currency,
          intent: 'capture'
        });
        
        renderPayPalButton();
      } catch (error) {
        console.error('Failed to load PayPal SDK:', error);
        onError('Failed to load PayPal SDK');
      }
    };

    const renderPayPalButton = () => {
      try {
        if (!window.paypal || !paypalRef.current || isRendered) {
          console.error('PayPal SDK not loaded, ref not available, or already rendered');
          return;
        }

        console.log('Rendering PayPal button (simple version)...');
        paypalRef.current.innerHTML = '';
        
        // Clean up any existing PayPal components to prevent conflicts
        cleanupPayPalComponents();

        window.paypal
          .Buttons({
            style: {
              layout: 'vertical',
              color: 'blue',
              shape: 'rect',
              label: 'pay',
              height: 55,
              tagline: false,
            },
            createOrder: async () => {
              try {
                const response = await api.createPayPalOrder({
                  patientId,
                  doctorId,
                  serviceId,
                  slotId,
                });
                return response.orderID;
              } catch (error: any) {
                console.error('Error creating PayPal order:', error);
                onError(error.message || 'Failed to create payment order');
                throw error;
              }
            },
            onApprove: async (data: any) => {
              try {
                console.log('PayPal onApprove called with data:', data);
                const response = await api.capturePayPalOrder(data.orderID);
                console.log('Capture response:', response);
                if (response.success) {
                  onSuccess(data.orderID);
                } else {
                  onError(response.message || 'Payment capture failed');
                }
              } catch (error: any) {
                console.error('Error capturing PayPal order:', error);
                onError(error.message || 'Payment processing failed');
              }
            },
            onCancel: () => {
              onCancel();
            },
            onError: (err: any) => {
              console.error('PayPal SDK error:', err);
              onError(`Payment processing error: ${err?.message || 'Unknown PayPal error'}`);
            },
          })
          .render(paypalRef.current)
          .then(() => {
            console.log('PayPal button rendered successfully (simple version)');
            setIsRendered(true);
            setIsLoading(false);
          })
          .catch((error: any) => {
            console.error('Error rendering PayPal button:', error);
            
            // If it's a zoid error, provide helpful message
            if (error.message && error.message.includes('zoid')) {
              console.log('Zoid error detected in simple version');
              onError('PayPal initialization failed. Please refresh the page.');
            } else {
              onError('Failed to initialize payment system');
            }
          });
      } catch (error: any) {
        console.error('Error in renderPayPalButton:', error);
        onError('Failed to initialize payment system');
      }
    };

    initializePayPal();

    // Cleanup function
    return () => {
      // Clean up PayPal components on unmount
      cleanupPayPalComponents();
      
      // Clear the container
      if (paypalRef.current) {
        paypalRef.current.innerHTML = '';
      }
    };
  }, [paypalClientId, currency, patientId, doctorId, serviceId, slotId, onSuccess, onError, onCancel]);

  // Reset rendered state when key props change
  useEffect(() => {
    setIsRendered(false);
  }, [patientId, doctorId, serviceId, slotId, amount]);

  // Development helper: Add global function to reset PayPal
  useEffect(() => {
    if (process.env.NODE_ENV === 'development') {
      (window as any).resetPayPal = () => {
        setIsRendered(false);
        setIsLoading(true);
        if (paypalRef.current) {
          paypalRef.current.innerHTML = '';
        }
        cleanupPayPalComponents();
        console.log('PayPal reset for development');
      };
    }
  }, []);

  return (
    <div className="relative z-20 w-full space-y-6">
      {/* Payment Summary Card */}
      <div className="relative overflow-hidden rounded-2xl border border-violet-100 dark:border-violet-800/30 bg-gradient-to-br from-violet-50 via-white to-cyan-50 dark:from-violet-950/50 dark:via-slate-900 dark:to-cyan-950/50 p-6 shadow-lg dark:shadow-violet-900/20">
        <div className="absolute inset-0 bg-gradient-to-br from-violet-500/5 to-cyan-500/5 dark:from-violet-400/10 dark:to-cyan-400/10"></div>
        <div className="relative">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-r from-violet-500 to-cyan-500 dark:from-violet-400 dark:to-cyan-400">
                  <svg className="h-4 w-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <h4 className="text-lg font-semibold text-slate-900 dark:text-slate-100">Secure Payment</h4>
              </div>
              <p className="text-sm text-slate-600 dark:text-slate-400">
                Virtual consultation fee
              </p>
            </div>
            <div className="text-right">
              <div className="text-2xl font-bold bg-gradient-to-r from-violet-600 to-cyan-600 dark:from-violet-400 dark:to-cyan-400 bg-clip-text text-transparent">
                ${amount.toFixed(2)}
              </div>
              <div className="text-xs text-slate-500 dark:text-slate-400 uppercase tracking-wide">
                {currency}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Payment Method Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          <div className="h-px flex-1 bg-gradient-to-r from-transparent via-slate-200 dark:via-slate-700 to-transparent"></div>
          <span className="text-sm font-medium text-slate-500 dark:text-slate-400">Choose Payment Method</span>
          <div className="h-px flex-1 bg-gradient-to-r from-transparent via-slate-200 dark:via-slate-700 to-transparent"></div>
        </div>

        {isLoading && (
          <div className="flex items-center justify-center py-12">
            <div className="relative">
              <div className="animate-spin rounded-full h-12 w-12 border-4 border-violet-100 dark:border-violet-800/50"></div>
              <div className="animate-spin rounded-full h-12 w-12 border-4 border-t-violet-500 dark:border-t-violet-400 absolute inset-0"></div>
            </div>
            <div className="ml-4 space-y-1">
              <div className="text-slate-600 dark:text-slate-300 font-medium">Loading secure payment options...</div>
              <div className="text-xs text-slate-500 dark:text-slate-400">Initializing PayPal checkout</div>
            </div>
          </div>
        )}

        {/* PayPal Button Container */}
        <div className={`${isLoading ? 'hidden' : 'block'} space-y-4`}>
          <div className="relative rounded-xl border border-slate-200 dark:border-slate-700/50 bg-white dark:bg-slate-800 p-6 shadow-lg">
            <div className="mb-4">
              <h3 className="text-sm font-semibold text-slate-700 dark:text-slate-200">Secure Payment with PayPal</h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">Simple version for testing</p>
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
          <span className="font-medium">256-bit SSL encryption • PCI DSS compliant • Your data is secure</span>
        </div>
      </div>
    </div>
  );
}