'use client';

/**
 * Error Boundary Component
 * 
 * Catches React errors and displays fallback UI
 */

import React, { Component, ReactNode } from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  section?: string;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    // Log error to console
    console.error('ErrorBoundary caught an error:', error, errorInfo);

    // Log to analytics if available
    if (typeof window !== 'undefined' && (window as any).gtag) {
      (window as any).gtag('event', 'exception', {
        description: `${this.props.section || 'Component'} error: ${error.message}`,
        fatal: false,
      });
    }
  }

  handleReset = () => {
    this.setState({ hasError: false, error: undefined });
  };

  render() {
    if (this.state.hasError) {
      // Use custom fallback if provided
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // Default error UI
      return (
        <div className="min-h-[400px] flex items-center justify-center p-8">
          <div className="max-w-md w-full bg-red-50 border border-red-200 rounded-lg p-6 text-center">
            <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              Something went wrong
            </h3>
            <p className="text-gray-600 mb-4">
              {this.props.section 
                ? `We encountered an error loading the ${this.props.section} section.`
                : 'We encountered an unexpected error.'
              }
            </p>
            <button
              onClick={this.handleReset}
              className="inline-flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
            >
              <RefreshCw className="w-4 h-4" />
              Try Again
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

/**
 * Lightweight error fallback for non-critical sections
 */
export function ErrorFallback({ 
  section, 
  onRetry 
}: { 
  section?: string; 
  onRetry?: () => void;
}) {
  return (
    <div className="py-12 px-4 text-center">
      <AlertCircle className="w-8 h-8 text-gray-400 mx-auto mb-3" />
      <p className="text-gray-600 mb-3">
        {section 
          ? `Unable to load ${section} content`
          : 'Content temporarily unavailable'
        }
      </p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="text-sm text-blue-600 hover:text-blue-700 underline"
        >
          Try again
        </button>
      )}
    </div>
  );
}
