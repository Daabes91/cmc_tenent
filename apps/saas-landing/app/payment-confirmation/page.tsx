'use client';

import { useEffect, useState, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import { Loader2, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { API_BASE_URL } from '@/lib/constants';

type ConfirmationStatus = 'loading' | 'success' | 'error' | 'invalid';

function PaymentConfirmationContent() {
  const searchParams = useSearchParams();
  const [status, setStatus] = useState<ConfirmationStatus>('loading');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [redirectUrl, setRedirectUrl] = useState<string>('');

  useEffect(() => {
    const confirmPayment = async () => {
      // Extract subscription_id from URL query parameters
      const subscriptionId = searchParams.get('subscription_id');
      const token = searchParams.get('token');

      if (!subscriptionId) {
        setStatus('invalid');
        setErrorMessage('Missing subscription information. Please contact support.');
        return;
      }

      try {
        // Call payment confirmation endpoint
        const response = await fetch(
          `${API_BASE_URL}/api/public/payment-confirmation?subscription_id=${encodeURIComponent(subscriptionId)}${token ? `&token=${encodeURIComponent(token)}` : ''}`,
          {
            method: 'GET',
            headers: {
              'Content-Type': 'application/json',
            },
          }
        );

        const data = await response.json();

        if (response.ok && data.success) {
          setStatus('success');
          
          // Store session token
          if (data.sessionToken) {
            // Store in localStorage
            localStorage.setItem('authToken', data.sessionToken);
            
            // Also store in cookie for SSR
            document.cookie = `authToken=${data.sessionToken}; path=/; max-age=86400; SameSite=Strict`;
          }

          // Set redirect URL
          if (data.redirectUrl) {
            setRedirectUrl(data.redirectUrl);
            
            // Redirect after 2 seconds
            setTimeout(() => {
              window.location.href = data.redirectUrl;
            }, 2000);
          }
        } else {
          setStatus('error');
          setErrorMessage(
            data.error || 
            'Payment verification failed. Please contact support if you were charged.'
          );
        }
      } catch (error) {
        console.error('Payment confirmation error:', error);
        setStatus('error');
        setErrorMessage(
          'Unable to verify your payment. Please check your internet connection or contact support.'
        );
      }
    };

    confirmPayment();
  }, [searchParams]);

  const renderContent = () => {
    switch (status) {
      case 'loading':
        return (
          <div className="text-center space-y-6">
            <div className="flex justify-center">
              <div className="relative">
                <Loader2 className="h-16 w-16 animate-spin text-primary" />
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="h-12 w-12 rounded-full bg-primary/10" />
                </div>
              </div>
            </div>
            <div className="space-y-2">
              <h1 className="text-2xl font-bold text-foreground">
                Verifying Your Payment
              </h1>
              <p className="text-muted-foreground">
                Please wait while we confirm your subscription...
              </p>
            </div>
          </div>
        );

      case 'success':
        return (
          <div className="text-center space-y-6">
            <div className="flex justify-center">
              <div className="relative">
                <CheckCircle className="h-16 w-16 text-green-600" />
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="h-20 w-20 rounded-full bg-green-600/10 animate-ping" />
                </div>
              </div>
            </div>
            <div className="space-y-2">
              <h1 className="text-2xl font-bold text-foreground">
                Payment Successful!
              </h1>
              <p className="text-muted-foreground">
                Your clinic portal has been activated. Redirecting you to onboarding...
              </p>
            </div>
            {redirectUrl && (
              <div className="pt-4">
                <Button
                  size="lg"
                  onClick={() => window.location.href = redirectUrl}
                  className="min-w-[200px]"
                >
                  Continue to Dashboard
                </Button>
              </div>
            )}
          </div>
        );

      case 'error':
        return (
          <div className="text-center space-y-6">
            <div className="flex justify-center">
              <XCircle className="h-16 w-16 text-destructive" />
            </div>
            <div className="space-y-2">
              <h1 className="text-2xl font-bold text-foreground">
                Payment Verification Failed
              </h1>
              <p className="text-muted-foreground max-w-md mx-auto">
                {errorMessage}
              </p>
            </div>
            <div className="flex flex-col sm:flex-row gap-3 justify-center pt-4">
              <Button
                variant="outline"
                size="lg"
                onClick={() => window.location.href = '/'}
              >
                Return to Home
              </Button>
              <Button
                size="lg"
                onClick={() => window.location.href = 'mailto:support@clinic.com'}
              >
                Contact Support
              </Button>
            </div>
          </div>
        );

      case 'invalid':
        return (
          <div className="text-center space-y-6">
            <div className="flex justify-center">
              <AlertCircle className="h-16 w-16 text-yellow-600" />
            </div>
            <div className="space-y-2">
              <h1 className="text-2xl font-bold text-foreground">
                Invalid Payment Link
              </h1>
              <p className="text-muted-foreground max-w-md mx-auto">
                {errorMessage}
              </p>
            </div>
            <div className="pt-4">
              <Button
                size="lg"
                onClick={() => window.location.href = '/'}
              >
                Return to Home
              </Button>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-gray-950 p-4">
      <div className="w-full max-w-2xl">
        <div className="bg-background rounded-lg border border-border shadow-lg p-8 sm:p-12">
          {renderContent()}
        </div>

        {/* Additional info */}
        <div className="mt-8 text-center">
          <p className="text-xs text-muted-foreground">
            Having trouble? Contact us at{' '}
            <a
              href="mailto:support@clinic.com"
              className="text-primary hover:underline"
            >
              support@clinic.com
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}

export default function PaymentConfirmationPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-gray-950">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      }
    >
      <PaymentConfirmationContent />
    </Suspense>
  );
}
