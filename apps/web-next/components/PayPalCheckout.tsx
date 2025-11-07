'use client';

import { useEffect, useRef, useState } from 'react';
import { api } from '@/lib/api';
import { usePayPalTheme } from '@/hooks/usePayPalTheme';

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

function PayPalCheckoutInner({
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
  const initializationRef = useRef<boolean>(false);
  const scriptLoadedRef = useRef<boolean>(false);
  const [isLoading, setIsLoading] = useState(true);
  const [paypalClientId, setPaypalClientId] = useState<string>('');
  const { mounted, isDarkMode, themeTransition, getPayPalButtonStyle, getThemeColors } = usePayPalTheme();

  console.log('PayPal component render - mounted:', mounted, 'isDarkMode:', isDarkMode, 'initialized:', initializationRef.current);

  useEffect(() => {
    const loadPayPalSettings = async () => {
      try {
        console.log('Loading PayPal settings...');
        const settings = await api.getPaymentSettings();
        console.log('PayPal settings loaded:', settings);
        setPaypalClientId(settings.paypalClientId);
      } catch (error) {
        console.error('Failed to load PayPal settings:', error);
        // For development, let's use a fallback client ID if the API fails
        if (process.env.NODE_ENV === 'development') {
          console.warn('Using fallback PayPal client ID for development');
          setPaypalClientId('AbJKTdpggZ_JdoLuIDgJcxekbnqZv8bZXb5ZtWYwDec7C2Hh022Kzu-I7ALyrbw5FsV6gzNcGf0i7Bnn');
        } else {
          onError('Failed to load payment configuration');
        }
      }
    };

    loadPayPalSettings();
  }, [onError]);

  useEffect(() => {
    if (!paypalClientId || !mounted || initializationRef.current) return;

    console.log('Initializing PayPal with clientId:', paypalClientId);
    initializationRef.current = true;

    // Monitor for PayPal modal creation and fix z-index issues
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        mutation.addedNodes.forEach((node) => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            const element = node as Element;
            // Check if this is a PayPal modal or overlay
            if (element.id?.includes('paypal') || 
                element.className?.includes('paypal') ||
                element.tagName === 'IFRAME' && element.getAttribute('src')?.includes('paypal')) {
              (element as HTMLElement).style.zIndex = '2147483647';
            }
            // Also check child elements
            const paypalElements = element.querySelectorAll('[id*="paypal"], [class*="paypal"], iframe[src*="paypal"]');
            paypalElements.forEach((el) => {
              (el as HTMLElement).style.zIndex = '2147483647';
            });
          }
        });
      });
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true
    });

    const loadPayPalScript = () => {
      if (window.paypal && scriptLoadedRef.current) {
        renderPayPalButton();
        return;
      }

      if (scriptLoadedRef.current) {
        console.log('Script already loaded, waiting for PayPal...');
        return;
      }

      // Check if script already exists
      const existingScript = document.querySelector('script[src*="paypal.com/sdk/js"]');
      if (existingScript) {
        console.log('PayPal script already exists, waiting for load...');
        scriptLoadedRef.current = true;
        // Wait for it to load
        const checkPayPal = setInterval(() => {
          if (window.paypal) {
            clearInterval(checkPayPal);
            renderPayPalButton();
          }
        }, 100);
        return;
      }

      console.log('Loading PayPal script...');
      const script = document.createElement('script');
      script.src = `https://www.paypal.com/sdk/js?client-id=${paypalClientId}&currency=${currency}&intent=capture`;
      script.async = true;
      script.onload = () => {
        console.log('PayPal script loaded');
        scriptLoadedRef.current = true;
        renderPayPalButton();
      };
      script.onerror = () => {
        console.error('Failed to load PayPal SDK script');
        scriptLoadedRef.current = false;
        initializationRef.current = false;
        onError('Failed to load PayPal SDK');
      };

      document.head.appendChild(script);
    };

    const renderPayPalButton = () => {
      try {
        if (!window.paypal || !paypalRef.current) {
          console.error('PayPal SDK not loaded or ref not available');
          return;
        }

        // Check if already rendered in this container
        if (paypalRef.current.children.length > 0) {
          console.log('PayPal button already rendered in this container');
          setIsLoading(false);
          return;
        }

        console.log('Rendering PayPal button...');

        // Clear any existing buttons
        paypalRef.current.innerHTML = '';
        
        // Destroy any existing PayPal components to prevent conflicts
        if (window.paypal && window.paypal.destroyAll) {
          try {
            window.paypal.destroyAll();
            console.log('Destroyed existing PayPal components');
          } catch (e) {
            console.warn('Could not destroy existing PayPal components:', e);
          }
        }

        // Add comprehensive custom styles for PayPal button and modal with theme support
        const styleId = 'paypal-custom-theme-styles';
        let style = document.getElementById(styleId) as HTMLStyleElement;
        
        if (!style) {
          style = document.createElement('style');
          style.id = styleId;
          document.head.appendChild(style);
        }
        
        const currentTheme = isDarkMode ? 'dark' : 'light';
        
        // Fallback theme colors in case hook isn't ready
        let themeColors;
        try {
          themeColors = getThemeColors();
        } catch (error) {
          console.warn('Theme colors not available, using fallback');
          themeColors = {
            primary: '#6366f1',
            secondary: '#0ea5e9',
            background: 'rgba(255, 255, 255, 0.95)',
            overlay: 'rgba(0, 0, 0, 0.5)',
            border: 'rgba(203, 213, 225, 0.5)',
            text: '#0f172a',
            textSecondary: '#64748b',
          };
        }
      
      style.textContent = `
        /* PayPal Button Styling */
        .paypal-button-container iframe {
          border-radius: 12px !important;
          transition: all 0.3s ease !important;
        }
        
        .paypal-button-container [data-funding-source="paypal"] {
          border-radius: 12px !important;
          transition: all 0.3s ease !important;
        }
        
        /* Enhanced PayPal Button Hover Effects */
        .paypal-button-container:hover iframe {
          transform: translateY(-1px) !important;
          box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
        }
        
        /* Comprehensive PayPal Modal Z-Index Management */
        .paypal-checkout-sandbox,
        .paypal-checkout-production,
        .paypal-modal,
        .paypal-checkout-modal,
        .paypal-overlay,
        [data-paypal-checkout],
        [data-paypal-checkout-sandbox],
        [data-paypal-checkout-production],
        iframe[name*="paypal"],
        iframe[src*="paypal"],
        iframe[id*="paypal"],
        div[id*="paypal"],
        div[class*="paypal"] {
          z-index: 2147483647 !important;
        }
        
        /* PayPal Modal Overlay - Dynamic Theme Support */
        .paypal-overlay,
        .paypal-checkout-overlay {
          z-index: 2147483647 !important;
          position: fixed !important;
          top: 0 !important;
          left: 0 !important;
          width: 100vw !important;
          height: 100vh !important;
          backdrop-filter: blur(8px) !important;
          transition: all 0.3s ease !important;
          background: ${themeColors.overlay} !important;
        }
        
        /* PayPal Modal Container - Theme Adaptive */
        .paypal-checkout-sandbox,
        .paypal-checkout-production,
        [data-paypal-checkout] {
          border-radius: 20px !important;
          overflow: hidden !important;
          backdrop-filter: blur(20px) !important;
          transition: all 0.3s ease !important;
          border: 1px solid ${themeColors.border} !important;
          box-shadow: ${currentTheme === 'dark'
            ? '0 25px 50px -12px rgba(0, 0, 0, 0.7), 0 0 0 1px rgba(148, 163, 184, 0.1)'
            : '0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 0 1px rgba(203, 213, 225, 0.3)'
          } !important;
          background: ${themeColors.background} !important;
        }
        
        /* PayPal Modal Content Theming */
        .paypal-checkout-sandbox *,
        .paypal-checkout-production *,
        [data-paypal-checkout] * {
          ${currentTheme === 'dark'
            ? 'color-scheme: dark !important;'
            : 'color-scheme: light !important;'
          }
        }
        
        /* PayPal Modal Animation */
        .paypal-checkout-sandbox,
        .paypal-checkout-production,
        [data-paypal-checkout] {
          animation: paypalModalSlideIn 0.3s ease-out !important;
        }
        
        @keyframes paypalModalSlideIn {
          from {
            opacity: 0;
            transform: scale(0.95) translateY(20px);
          }
          to {
            opacity: 1;
            transform: scale(1) translateY(0);
          }
        }
        
        /* Header Z-Index Management */
        header,
        nav {
          z-index: 2147483648 !important;
          position: relative !important;
        }
        
        /* Body State Management */
        body.paypal-modal-open {
          overflow: hidden !important;
        }
        
        body.paypal-modal-active header {
          z-index: 2147483648 !important;
          position: sticky !important;
        }
        
        /* PayPal Button Container Enhanced Styling */
        .paypal-button-container {
          border-radius: 12px !important;
          overflow: hidden !important;
          position: relative !important;
          ${currentTheme === 'dark'
            ? 'background: rgba(30, 41, 59, 0.5) !important;'
            : 'background: rgba(248, 250, 252, 0.5) !important;'
          }
          backdrop-filter: blur(10px) !important;
          border: 1px solid ${currentTheme === 'dark'
            ? 'rgba(148, 163, 184, 0.1)'
            : 'rgba(203, 213, 225, 0.3)'
          } !important;
        }
        
        /* PayPal Loading State */
        .paypal-button-container.loading {
          opacity: 0.7 !important;
          pointer-events: none !important;
        }
        
        /* Custom PayPal Modal Close Button */
        .paypal-modal-close {
          position: absolute !important;
          top: 16px !important;
          right: 16px !important;
          width: 32px !important;
          height: 32px !important;
          border-radius: 50% !important;
          ${currentTheme === 'dark'
            ? 'background: rgba(51, 65, 85, 0.8) !important; color: rgba(248, 250, 252, 0.9) !important;'
            : 'background: rgba(248, 250, 252, 0.8) !important; color: rgba(51, 65, 85, 0.9) !important;'
          }
          border: none !important;
          cursor: pointer !important;
          display: flex !important;
          align-items: center !important;
          justify-content: center !important;
          font-size: 18px !important;
          font-weight: bold !important;
          z-index: 2147483648 !important;
          transition: all 0.2s ease !important;
        }
        
        .paypal-modal-close:hover {
          transform: scale(1.1) !important;
          ${currentTheme === 'dark'
            ? 'background: rgba(51, 65, 85, 1) !important;'
            : 'background: rgba(248, 250, 252, 1) !important;'
          }
        }
        
        /* PayPal Iframe Content Theming */
        iframe[src*="paypal"] {
          ${currentTheme === 'dark'
            ? 'filter: brightness(0.9) contrast(1.1) !important;'
            : 'filter: brightness(1) contrast(1) !important;'
          }
        }
        
        /* Responsive PayPal Modal */
        @media (max-width: 768px) {
          .paypal-checkout-sandbox,
          .paypal-checkout-production,
          [data-paypal-checkout] {
            margin: 10px !important;
            border-radius: 16px !important;
            max-width: calc(100vw - 20px) !important;
            max-height: calc(100vh - 20px) !important;
          }
        }
        
        /* PayPal Button Focus States */
        .paypal-button-container button:focus,
        .paypal-button-container iframe:focus {
          outline: 2px solid ${currentTheme === 'dark'
            ? 'rgba(139, 92, 246, 0.6)'
            : 'rgba(99, 102, 241, 0.6)'
          } !important;
          outline-offset: 2px !important;
        }
      `;
      
      // Apply theme class to body for PayPal modal detection
      document.body.setAttribute('data-theme', currentTheme);

        // Fallback button style in case hook isn't ready
        let buttonStyle;
        try {
          buttonStyle = getPayPalButtonStyle();
        } catch (error) {
          console.warn('PayPal button style not available, using fallback');
          buttonStyle = {
            layout: 'vertical' as const,
            color: 'blue' as const,
            shape: 'rect' as const,
            label: 'pay' as const,
            height: 55,
            tagline: false,
            fundingicons: false,
          };
        }
        console.log('PayPal button style:', buttonStyle);

        window.paypal
          .Buttons({
            style: buttonStyle,
            // Enhanced PayPal configuration for better theme integration
            env: 'sandbox', // Change to 'production' for live
            locale: 'en_US',
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
                // Add class to body to indicate PayPal modal is active
                document.body.classList.add('paypal-modal-active');
                
                const response = await api.capturePayPalOrder(data.orderID);
                console.log('Capture response:', response);
                if (response.success) {
                  onSuccess(data.orderID);
                } else {
                  onError(response.message || 'Payment capture failed');
                }
                
                // Remove class when done
                document.body.classList.remove('paypal-modal-active');
              } catch (error: any) {
                console.error('Error capturing PayPal order:', error);
                console.error('Capture error details:', JSON.stringify(error, null, 2));
                document.body.classList.remove('paypal-modal-active');
                onError(error.message || 'Payment processing failed');
              }
            },
            onCancel: () => {
              document.body.classList.remove('paypal-modal-active');
              onCancel();
            },
            onError: (err: any) => {
              console.error('PayPal SDK error:', err);
              console.error('PayPal error details:', JSON.stringify(err, null, 2));
              document.body.classList.remove('paypal-modal-active');
              onError(`Payment processing error: ${err?.message || 'Unknown PayPal error'}`);
            },
          })
          .render(paypalRef.current)
          .then(() => {
            console.log('PayPal button rendered successfully');
            setIsLoading(false);
          })
          .catch((error: any) => {
            console.error('Error rendering PayPal button:', error);
            console.error('PayPal render error details:', JSON.stringify(error, null, 2));
            
            // Reset initialization state on error
            initializationRef.current = false;
            scriptLoadedRef.current = false;
            
            // If it's a zoid error, provide helpful message
            if (error.message && error.message.includes('zoid')) {
              console.log('Zoid error detected - this usually means multiple PayPal instances');
              onError('PayPal initialization conflict detected. Please refresh the page to reset.');
            } else {
              onError('Failed to initialize payment system');
            }
          });
      } catch (error: any) {
        console.error('Error in renderPayPalButton:', error);
        onError('Failed to initialize payment system');
      }
    };

    loadPayPalScript();

    loadPayPalScript();

    // Cleanup function
    return () => {
      observer.disconnect();
      
      // Clean up PayPal components on unmount
      if (window.paypal && window.paypal.destroyAll) {
        try {
          window.paypal.destroyAll();
        } catch (e) {
          console.warn('Could not destroy PayPal components on unmount:', e);
        }
      }
      
      // Clear the container
      if (paypalRef.current) {
        paypalRef.current.innerHTML = '';
      }
      
      // Reset refs
      initializationRef.current = false;
      scriptLoadedRef.current = false;
    };
  }, [paypalClientId, currency, patientId, doctorId, serviceId, slotId, onSuccess, onError, onCancel, mounted, isDarkMode]);

  // Reset initialization state when key props change
  useEffect(() => {
    initializationRef.current = false;
    if (paypalRef.current) {
      paypalRef.current.innerHTML = '';
    }
  }, [patientId, doctorId, serviceId, slotId, amount]);

  return (
    <div className={`relative z-20 w-full space-y-6 transition-all duration-300 ${themeTransition ? 'opacity-90' : 'opacity-100'}`}>
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
              <div className="text-3xl font-bold bg-gradient-to-r from-violet-600 to-cyan-600 dark:from-violet-400 dark:to-cyan-400 bg-clip-text text-transparent">
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
              <div className="absolute inset-0 rounded-full bg-gradient-to-r from-violet-500/20 to-cyan-500/20 dark:from-violet-400/20 dark:to-cyan-400/20 animate-pulse"></div>
            </div>
            <div className="ml-4 space-y-1">
              <div className="text-slate-600 dark:text-slate-300 font-medium">Loading secure payment options...</div>
              <div className="text-xs text-slate-500 dark:text-slate-400">Initializing PayPal checkout</div>
            </div>
          </div>
        )}

        {/* PayPal Button Container */}
        <div className={`${isLoading ? 'hidden' : 'block'} space-y-4`}>
          <div className="relative z-[2147483649] rounded-xl border border-slate-200 dark:border-slate-700/50 bg-gradient-to-br from-white via-slate-50/50 to-white dark:from-slate-800 dark:via-slate-800/80 dark:to-slate-900 p-6 shadow-lg dark:shadow-slate-900/30 backdrop-blur-sm">
            <div className="absolute inset-0 bg-gradient-to-br from-violet-500/5 via-transparent to-cyan-500/5 dark:from-violet-400/10 dark:via-transparent dark:to-cyan-400/10 rounded-xl"></div>
            
            <div className="relative space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-r from-blue-500 to-blue-600 dark:from-blue-400 dark:to-blue-500 shadow-lg">
                    <svg className="h-5 w-5 text-white" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M7.076 21.337H2.47a.641.641 0 0 1-.633-.74L4.944.901C5.026.382 5.474 0 5.998 0h8.418c2.508 0 4.684.942 5.572 2.415.888 1.473.73 3.377-.398 4.802.748 1.26.814 3.24-.231 4.66-1.045 1.42-3.026 2.124-5.24 2.124h-1.621L10.9 21.338a.641.641 0 0 1-.633.74H7.076z"/>
                    </svg>
                  </div>
                  <div>
                    <div className="text-sm font-semibold text-slate-700 dark:text-slate-200">Secure Payment with PayPal</div>
                    <div className="text-xs text-slate-500 dark:text-slate-400">
                      {mounted && (
                        <span className="inline-flex items-center gap-1">
                          <div className={`w-2 h-2 rounded-full ${isDarkMode ? 'bg-slate-400' : 'bg-slate-600'}`}></div>
                          {isDarkMode ? 'Dark' : 'Light'} theme optimized
                        </span>
                      )}
                    </div>
                  </div>
                </div>
                
                <div className="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
                  <svg className="h-4 w-4 text-green-500 dark:text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span>SSL Secured</span>
                </div>
              </div>
              
              <div ref={paypalRef} className="paypal-button-container relative z-[2147483649] transition-all duration-300"></div>
              
              <div className="flex items-center justify-center gap-4 text-xs text-slate-500 dark:text-slate-400 pt-2 border-t border-slate-200 dark:border-slate-700">
                <div className="flex items-center gap-1">
                  <svg className="h-3 w-3" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
                  </svg>
                  <span>Bank-level security</span>
                </div>
                <div className="flex items-center gap-1">
                  <svg className="h-3 w-3" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
                  </svg>
                  <span>256-bit encryption</span>
                </div>
                <div className="flex items-center gap-1">
                  <svg className="h-3 w-3" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                  </svg>
                  <span>PCI compliant</span>
                </div>
              </div>
            </div>
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
        <div className="mt-2 flex items-center justify-center gap-4 text-xs text-slate-500 dark:text-slate-500">
          <div className="flex items-center gap-1">
            <svg className="h-3 w-3" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
            <span>Encrypted</span>
          </div>
          <div className="flex items-center gap-1">
            <svg className="h-3 w-3" fill="currentColor" viewBox="0 0 24 24">
              <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
            </svg>
            <span>Protected</span>
          </div>
          <div className="flex items-center gap-1">
            <svg className="h-3 w-3" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
            </svg>
            <span>Verified</span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function PayPalCheckout(props: PayPalCheckoutProps) {
  return (
    <div>
      <PayPalCheckoutInner {...props} />
    </div>
  );
}