'use client';

import { useState, useEffect } from 'react';
import { api } from '@/lib/api';
import { usePayPalTheme } from '@/hooks/usePayPalTheme';

export default function PayPalDebugTest() {
  const [paymentSettings, setPaymentSettings] = useState<any>(null);
  const [error, setError] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const themeHook = usePayPalTheme();

  useEffect(() => {
    const testAPI = async () => {
      try {
        console.log('Testing payment settings API...');
        const settings = await api.getPaymentSettings();
        console.log('Payment settings response:', settings);
        setPaymentSettings(settings);
      } catch (err: any) {
        console.error('API Error:', err);
        setError(err.message || 'Unknown error');
      } finally {
        setLoading(false);
      }
    };

    testAPI();
  }, []);

  return (
    <div className="p-8 max-w-2xl mx-auto space-y-6">
      <h1 className="text-xl font-bold">PayPal Debug Test</h1>
      
      {/* API Test */}
      <div className="border rounded-lg p-4">
        <h2 className="text-lg font-semibold mb-2">API Test</h2>
        {loading && <p>Loading payment settings...</p>}
        {error && <p className="text-red-500">Error: {error}</p>}
        {paymentSettings && (
          <div className="bg-gray-100 dark:bg-gray-800 p-3 rounded">
            <pre>{JSON.stringify(paymentSettings, null, 2)}</pre>
          </div>
        )}
      </div>

      {/* Theme Hook Test */}
      <div className="border rounded-lg p-4">
        <h2 className="text-lg font-semibold mb-2">Theme Hook Test</h2>
        <div className="space-y-2">
          <p>Mounted: {themeHook.mounted ? 'Yes' : 'No'}</p>
          <p>Dark Mode: {themeHook.isDarkMode ? 'Yes' : 'No'}</p>
          <p>Theme: {themeHook.theme || 'Not set'}</p>
          <p>Transition: {themeHook.themeTransition ? 'Yes' : 'No'}</p>
        </div>
        
        {themeHook.mounted && (
          <div className="mt-4 space-y-2">
            <h3 className="font-medium">PayPal Button Style:</h3>
            <div className="bg-gray-100 dark:bg-gray-800 p-3 rounded">
              <pre>{JSON.stringify(themeHook.getPayPalButtonStyle(), null, 2)}</pre>
            </div>
            
            <h3 className="font-medium">Theme Colors:</h3>
            <div className="bg-gray-100 dark:bg-gray-800 p-3 rounded">
              <pre>{JSON.stringify(themeHook.getThemeColors(), null, 2)}</pre>
            </div>
          </div>
        )}
      </div>

      {/* PayPal SDK Test */}
      <div className="border rounded-lg p-4">
        <h2 className="text-lg font-semibold mb-2">PayPal SDK Test</h2>
        <button
          onClick={() => {
            if (typeof window !== 'undefined') {
              console.log('Window.paypal:', window.paypal);
              if (window.paypal) {
                console.log('PayPal SDK is loaded');
              } else {
                console.log('PayPal SDK is not loaded');
              }
            }
          }}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Check PayPal SDK
        </button>
      </div>
    </div>
  );
}