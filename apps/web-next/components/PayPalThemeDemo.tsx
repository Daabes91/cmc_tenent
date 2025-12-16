'use client';

import { useState } from 'react';
import { useTheme } from 'next-themes';
import PayPalCheckoutIsolated from './PayPalCheckoutIsolated';
import PayPalCheckoutSimple from './PayPalCheckoutSimple';

export default function PayPalThemeDemo() {
  const { theme, setTheme } = useTheme();
  const [showPayPal, setShowPayPal] = useState(false);
  const [useSimple, setUseSimple] = useState(false);

  const mockProps = {
    amount: 50.00,
    currency: 'USD',
    patientId: 1,
    doctorId: 1,
    serviceId: 1,
    slotId: '2024-01-15T10:00:00',
    onSuccess: (orderID: string) => {
      console.log('Payment successful:', orderID);
      alert('Payment successful!');
      setShowPayPal(false);
    },
    onError: (error: string) => {
      console.error('Payment error:', error);
      alert('Payment failed: ' + error);
    },
    onCancel: () => {
      console.log('Payment cancelled');
      setShowPayPal(false);
    },
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900 p-8 transition-all duration-500">
      <div className="max-w-4xl mx-auto space-y-8">
        {/* Header */}
        <div className="text-center space-y-4">
          <h1 className="text-2xl font-bold bg-gradient-to-r from-violet-600 to-cyan-600 dark:from-violet-400 dark:to-cyan-400 bg-clip-text text-transparent">
            PayPal Theme Demo
          </h1>
          <p className="text-slate-600 dark:text-slate-400 text-lg">
            Experience our PayPal integration with dynamic theme support
          </p>
        </div>

        {/* Theme Controls */}
        <div className="flex justify-center">
          <div className="inline-flex rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-1 shadow-lg">
            <button
              onClick={() => setTheme('light')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                theme === 'light'
                  ? 'bg-gradient-to-r from-violet-500 to-cyan-500 text-white shadow-md'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              ☀️ Light
            </button>
            <button
              onClick={() => setTheme('dark')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                theme === 'dark'
                  ? 'bg-gradient-to-r from-violet-500 to-cyan-500 text-white shadow-md'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              🌙 Dark
            </button>
            <button
              onClick={() => setTheme('system')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                theme === 'system'
                  ? 'bg-gradient-to-r from-violet-500 to-cyan-500 text-white shadow-md'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              💻 System
            </button>
          </div>
        </div>

        {/* Demo Controls */}
        <div className="text-center space-y-4">
          <div className="flex justify-center gap-4">
            <button
              onClick={() => setUseSimple(!useSimple)}
              className={`px-4 py-2 rounded-lg font-medium transition-all ${
                useSimple
                  ? 'bg-orange-500 text-white'
                  : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300'
              }`}
            >
              {useSimple ? 'Using Simple Version' : 'Use Simple Version'}
            </button>
          </div>
          
          <button
            onClick={() => setShowPayPal(!showPayPal)}
            className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-violet-500 to-cyan-500 hover:from-violet-600 hover:to-cyan-600 text-white font-semibold rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 transform hover:scale-105"
          >
            {showPayPal ? (
              <>
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
                Hide PayPal Checkout
              </>
            ) : (
              <>
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
                Show PayPal Checkout
              </>
            )}
          </button>
        </div>

        {/* PayPal Checkout Demo */}
        {showPayPal && (
          <div className="max-w-md mx-auto">
            <div className="rounded-2xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-6 shadow-xl">
              <div className="mb-6 text-center">
                <h3 className="text-xl font-semibold text-slate-900 dark:text-slate-100 mb-2">
                  Virtual Consultation
                </h3>
                <p className="text-slate-600 dark:text-slate-400 text-sm">
                  Demo payment with theme-aware PayPal integration
                </p>
              </div>
              
              {useSimple ? (
                <PayPalCheckoutSimple {...mockProps} />
              ) : (
                <PayPalCheckoutIsolated {...mockProps} />
              )}
            </div>
          </div>
        )}

        {/* Features */}
        <div className="grid md:grid-cols-3 gap-6 mt-12">
          <div className="text-center p-6 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 shadow-lg">
            <div className="w-12 h-12 mx-auto mb-4 bg-gradient-to-r from-violet-500 to-cyan-500 rounded-full flex items-center justify-center">
              <svg className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zM21 5a2 2 0 00-2-2h-4a2 2 0 00-2 2v12a4 4 0 004 4h4a2 2 0 002-2V5z" />
              </svg>
            </div>
            <h4 className="font-semibold text-slate-900 dark:text-slate-100 mb-2">Dynamic Theming</h4>
            <p className="text-sm text-slate-600 dark:text-slate-400">
              PayPal modal adapts to your theme preference automatically
            </p>
          </div>

          <div className="text-center p-6 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 shadow-lg">
            <div className="w-12 h-12 mx-auto mb-4 bg-gradient-to-r from-violet-500 to-cyan-500 rounded-full flex items-center justify-center">
              <svg className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <h4 className="font-semibold text-slate-900 dark:text-slate-100 mb-2">Secure Integration</h4>
            <p className="text-sm text-slate-600 dark:text-slate-400">
              Bank-level security with 256-bit SSL encryption
            </p>
          </div>

          <div className="text-center p-6 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 shadow-lg">
            <div className="w-12 h-12 mx-auto mb-4 bg-gradient-to-r from-violet-500 to-cyan-500 rounded-full flex items-center justify-center">
              <svg className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h4 className="font-semibold text-slate-900 dark:text-slate-100 mb-2">Smooth Transitions</h4>
            <p className="text-sm text-slate-600 dark:text-slate-400">
              Seamless theme transitions with optimized performance
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}