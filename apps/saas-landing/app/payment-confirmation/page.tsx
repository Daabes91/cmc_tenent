'use client';

import { useEffect, useState, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import { Loader2, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { API_BASE_URL } from '@/lib/constants';

interface PaymentConfirmationResponse {
  success: boolean;
  sessionToken?: string;
  redirectUrl?: string;
  error?: string;
}

// Persist session for admin panel so onboarding loads without forcing login
const bootstrapAdminSession = (token: string) => {
  try {
    const [, payloadB64] = token.split('.');
    const payloadJson = atob(payloadB64 || '');
    const payload = JSON.parse(payloadJson);
    const expSeconds = payload?.exp;
    const expiryIso = expSeconds ? new Date(expSeconds * 1000).toISOString() : null;

    localStorage.setItem('saas_auth_token', token);
    localStorage.setItem('saas_manager_name', payload?.name || 'Owner');
    if (expiryIso) {
      localStorage.setItem('saas_token_expiry', expiryIso);
    }
  } catch (e) {
    console.warn('Unable to decode session token; falling back to token-only storage', e);
    localStorage.setItem('saas_auth_token', token);
  }
};

function PaymentConfirmationContent() {
  const searchParams = useSearchParams();
  
  const [isVerifying, setIsVerifying] = useState(true);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [redirectUrl, setRedirectUrl] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);

  const subscriptionId = searchParams.get('subscription_id');
  const token = searchParams.get('token');

  useEffect(() => {
    // Validate required parameters
    if (!subscriptionId || !token) {
      setIsVerifying(false);
      setError('Missing payment confirmation parameters. Please contact support.');
      return;
    }

    // Call payment confirmation endpoint
    const confirmPayment = async () => {
      setIsVerifying(true);
      setError(null);

      try {
        const response = await fetch(
          `${API_BASE_URL}/api/public/payment-confirmation?subscription_id=${encodeURIComponent(subscriptionId)}&token=${encodeURIComponent(token)}`,
          {
            method: 'GET',
            headers: {
              'Content-Type': 'application/json',
            },
          }
        );

        const data: PaymentConfirmationResponse = await response.json();

          if (response.ok && data.success) {
            setSuccess(true);
            
            // Store session token in localStorage
            if (data.sessionToken) {
              localStorage.setItem('sessionToken', data.sessionToken);
              bootstrapAdminSession(data.sessionToken);
            }
            
            // Store redirect URL
            if (data.redirectUrl) {
              setRedirectUrl(data.redirectUrl);
            }
          } else {
            // Handle error response
            setError(data.error || 'Payment verification failed. Please try again.');
          }
      } catch (err) {
        console.error('Payment confirmation error:', err);
        setError('Unable to connect to the server. Please check your internet connection and try again.');
      } finally {
        setIsVerifying(false);
      }
    };

    confirmPayment();
  }, [subscriptionId, token, retryCount]);

  const handleRetry = () => {
    setRetryCount(prev => prev + 1);
  };

  const handleContactSupport = () => {
    // Redirect to support page or open email client
    window.location.href = 'mailto:support@cliniqax.com?subject=Payment Confirmation Issue&body=Subscription ID: ' + subscriptionId;
  };

  // Loading state
  if (isVerifying) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-gray-950">
        <div className="max-w-md w-full mx-4">
          <div className="bg-background rounded-lg border border-border shadow-lg p-8">
            <div className="flex flex-col items-center text-center">
              <Loader2 className="h-16 w-16 animate-spin text-primary mb-4" />
              <h2 className="text-2xl font-bold text-foreground mb-2">
                Verifying Your Payment
              </h2>
              <p className="text-muted-foreground">
                Please wait while we confirm your subscription with PayPal...
              </p>
              <div className="mt-6 w-full bg-muted rounded-full h-2 overflow-hidden">
                <div className="h-full bg-primary animate-pulse" style={{ width: '60%' }} />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Success state
  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-gray-950">
        <div className="max-w-md w-full mx-4">
          <div className="bg-background rounded-lg border border-border shadow-lg p-8">
            <div className="flex flex-col items-center text-center">
              <div className="rounded-full bg-green-100 dark:bg-green-900/20 p-3 mb-4">
                <CheckCircle className="h-16 w-16 text-green-600 dark:text-green-400" />
              </div>
              <h2 className="text-2xl font-bold text-foreground mb-2">
                Payment Confirmed!
              </h2>
              <p className="text-muted-foreground mb-6">
                Your subscription is active. Continue to your admin onboarding to finish setup.
              </p>
              {redirectUrl && (
                <div className="w-full">
                  <Button
                    onClick={() => window.location.href = redirectUrl}
                    className="w-full"
                    size="lg"
                  >
                    Go to Admin Onboarding
                  </Button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Error state
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-gray-950">
      <div className="max-w-md w-full mx-4">
        <div className="bg-background rounded-lg border border-border shadow-lg p-8">
          <div className="flex flex-col items-center text-center">
            <div className="rounded-full bg-red-100 dark:bg-red-900/20 p-3 mb-4">
              <XCircle className="h-16 w-16 text-red-600 dark:text-red-400" />
            </div>
            <h2 className="text-2xl font-bold text-foreground mb-2">
              Payment Verification Failed
            </h2>
            <div className="mb-6 p-4 bg-destructive/10 border border-destructive/20 rounded-md">
              <div className="flex items-start gap-3">
                <AlertCircle className="h-5 w-5 text-destructive flex-shrink-0 mt-0.5" />
                <p className="text-sm text-destructive text-left">
                  {error}
                </p>
              </div>
            </div>
            <div className="w-full space-y-3">
              <Button
                onClick={handleRetry}
                className="w-full"
                size="lg"
                variant="default"
              >
                <Loader2 className="mr-2 h-4 w-4" />
                Retry Verification
              </Button>
              <Button
                onClick={handleContactSupport}
                className="w-full"
                size="lg"
                variant="outline"
              >
                Contact Support
              </Button>
            </div>
            <p className="text-xs text-muted-foreground mt-4">
              If the problem persists, please contact our support team with your subscription ID: <span className="font-mono">{subscriptionId}</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function PaymentConfirmationPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-gray-950">
        <Loader2 className="h-16 w-16 animate-spin text-primary" />
      </div>
    }>
      <PaymentConfirmationContent />
    </Suspense>
  );
}
