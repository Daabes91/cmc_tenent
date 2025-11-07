// PayPal utility functions to manage script loading and component lifecycle

let paypalScriptLoaded = false;
let paypalScriptLoading = false;
let paypalScriptPromise: Promise<void> | null = null;

export interface PayPalScriptOptions {
  clientId: string;
  currency: string;
  intent?: string;
}

/**
 * Load PayPal script only once and return a promise
 */
export function loadPayPalScript(options: PayPalScriptOptions): Promise<void> {
  // If already loaded, resolve immediately
  if (paypalScriptLoaded && window.paypal) {
    return Promise.resolve();
  }

  // If currently loading, return the existing promise
  if (paypalScriptLoading && paypalScriptPromise) {
    return paypalScriptPromise;
  }

  // Start loading
  paypalScriptLoading = true;
  paypalScriptPromise = new Promise((resolve, reject) => {
    // Check if script already exists
    const existingScript = document.querySelector('script[src*="paypal.com/sdk/js"]');
    if (existingScript) {
      existingScript.remove();
    }

    const script = document.createElement('script');
    script.src = `https://www.paypal.com/sdk/js?client-id=${options.clientId}&currency=${options.currency}&intent=${options.intent || 'capture'}`;
    script.async = true;
    
    script.onload = () => {
      paypalScriptLoaded = true;
      paypalScriptLoading = false;
      console.log('PayPal script loaded successfully');
      resolve();
    };
    
    script.onerror = () => {
      paypalScriptLoading = false;
      paypalScriptPromise = null;
      console.error('Failed to load PayPal script');
      reject(new Error('Failed to load PayPal SDK'));
    };

    document.head.appendChild(script);
  });

  return paypalScriptPromise;
}

/**
 * Clean up PayPal components safely
 */
export function cleanupPayPalComponents(): void {
  try {
    if (window.paypal && typeof window.paypal.destroyAll === 'function') {
      window.paypal.destroyAll();
      console.log('PayPal components destroyed');
    }
  } catch (error) {
    console.warn('Error destroying PayPal components:', error);
  }
}

/**
 * Reset PayPal script state (useful for development/testing)
 */
export function resetPayPalScript(): void {
  paypalScriptLoaded = false;
  paypalScriptLoading = false;
  paypalScriptPromise = null;
  
  // Remove existing script
  const existingScript = document.querySelector('script[src*="paypal.com/sdk/js"]');
  if (existingScript) {
    existingScript.remove();
  }
  
  // Clean up components
  cleanupPayPalComponents();
  
  console.log('PayPal script state reset');
}

/**
 * Check if PayPal is ready to use
 */
export function isPayPalReady(): boolean {
  return paypalScriptLoaded && !!window.paypal;
}

/**
 * Wait for PayPal to be ready
 */
export function waitForPayPal(timeout = 10000): Promise<void> {
  return new Promise((resolve, reject) => {
    if (isPayPalReady()) {
      resolve();
      return;
    }

    const startTime = Date.now();
    const checkInterval = setInterval(() => {
      if (isPayPalReady()) {
        clearInterval(checkInterval);
        resolve();
      } else if (Date.now() - startTime > timeout) {
        clearInterval(checkInterval);
        reject(new Error('PayPal script loading timeout'));
      }
    }, 100);
  });
}