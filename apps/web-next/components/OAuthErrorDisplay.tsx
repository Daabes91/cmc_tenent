'use client';

import { useTranslations } from 'next-intl';
import { OAuthError, OAuthErrorType } from '@/lib/oauth-errors';

interface OAuthErrorDisplayProps {
  error: OAuthError;
  onRetry?: () => void;
  onDismiss?: () => void;
}

/**
 * Component for displaying OAuth errors with appropriate styling and actions
 */
export default function OAuthErrorDisplay({ 
  error, 
  onRetry, 
  onDismiss 
}: OAuthErrorDisplayProps) {
  const t = useTranslations();

  // Get icon based on error type
  const getIcon = () => {
    if (error.type === OAuthErrorType.USER_CANCELLED) {
      return (
        <svg className="w-10 h-10 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      );
    }

    if (error.type === OAuthErrorType.NETWORK_ERROR || error.type === OAuthErrorType.TIMEOUT) {
      return (
        <svg className="w-10 h-10 text-orange-600 dark:text-orange-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 5.636a9 9 0 010 12.728m0 0l-2.829-2.829m2.829 2.829L21 21M15.536 8.464a5 5 0 010 7.072m0 0l-2.829-2.829m-4.243 2.829a4.978 4.978 0 01-1.414-2.83m-1.414 5.658a9 9 0 01-2.167-9.238m7.824 2.167a1 1 0 111.414 1.414m-1.414-1.414L3 3m8.293 8.293l1.414 1.414" />
        </svg>
      );
    }

    return (
      <svg className="w-10 h-10 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    );
  };

  // Get background color based on error type
  const getBackgroundColor = () => {
    if (error.type === OAuthErrorType.USER_CANCELLED) {
      return 'bg-yellow-100 dark:bg-yellow-900/30';
    }

    if (error.type === OAuthErrorType.NETWORK_ERROR || error.type === OAuthErrorType.TIMEOUT) {
      return 'bg-orange-100 dark:bg-orange-900/30';
    }

    return 'bg-red-100 dark:bg-red-900/30';
  };

  // Get translated error message
  const errorMessage = t(error.userMessage, { 
    error: error.message 
  });

  return (
    <div className="text-center">
      {/* Error Icon */}
      <div className={`inline-flex items-center justify-center w-16 h-16 rounded-full ${getBackgroundColor()} mb-4`}>
        {getIcon()}
      </div>

      {/* Error Title */}
      <h1 className="text-xl font-bold text-slate-900 dark:text-slate-100 mb-2">
        {t('auth.google.callback.error.title')}
      </h1>

      {/* Error Message */}
      <p className="text-slate-600 dark:text-slate-300 mb-6">
        {errorMessage}
      </p>

      {/* Action Buttons */}
      <div className="flex flex-col sm:flex-row gap-3 justify-center">
        {error.retryable && onRetry && (
          <button
            onClick={onRetry}
            className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl transition-colors"
          >
            {t('common.retry', { default: 'Try Again' })}
          </button>
        )}
        
        {onDismiss && (
          <button
            onClick={onDismiss}
            className="px-6 py-3 bg-slate-200 hover:bg-slate-300 dark:bg-slate-700 dark:hover:bg-slate-600 text-slate-900 dark:text-slate-100 font-semibold rounded-xl transition-colors"
          >
            {t('common.dismiss', { default: 'Dismiss' })}
          </button>
        )}
      </div>

      {/* Additional Help Text */}
      {error.type !== OAuthErrorType.USER_CANCELLED && (
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-6">
          {t('auth.google.callback.error.redirect')}
        </p>
      )}
    </div>
  );
}
