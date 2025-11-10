'use client';

import { Component, ReactNode } from 'react';
import Image from 'next/image';

interface Props {
  children: ReactNode;
  fallbackImage?: string;
}

interface State {
  hasError: boolean;
  error?: Error;
}

/**
 * Error Boundary for Hero Section
 * 
 * Catches errors in the hero section and displays a fallback UI
 * to ensure the page continues to render normally even if hero media fails.
 * 
 * Features:
 * - Catches React errors in hero section
 * - Displays fallback image on error
 * - Logs errors for debugging
 * - Maintains page functionality
 */
export class HeroSectionErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    // Update state so the next render will show the fallback UI
    console.error('Hero section error boundary caught error:', error);
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    // Log error details for debugging
    console.error('Hero section error details:', {
      error: error.message,
      stack: error.stack,
      componentStack: errorInfo.componentStack,
      timestamp: new Date().toISOString(),
    });
  }

  render() {
    if (this.state.hasError) {
      // Fallback UI - display default image
      const fallbackImage = this.props.fallbackImage || 
        'https://images.unsplash.com/photo-1606811971618-4486d14f3f99?q=80&w=1200&auto=format&fit=crop';

      return (
        <div className="relative">
          <Image
            src={fallbackImage}
            alt="Hero section"
            width={1200}
            height={800}
            priority
            sizes="(max-width: 1024px) 100vw, 50vw"
            className="h-full w-full rounded-3xl border border-slate-100/60 object-cover shadow-2xl transition-colors dark:border-slate-800/60 dark:shadow-blue-900/50"
          />
          <div className="absolute inset-0 rounded-3xl bg-gradient-to-tr from-blue-600/10 dark:from-blue-500/20 to-transparent transition-colors" />
        </div>
      );
    }

    return this.props.children;
  }
}
