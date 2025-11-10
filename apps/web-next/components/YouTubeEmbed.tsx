'use client';

import { useState, useEffect, useRef } from 'react';

interface YouTubeEmbedProps {
  videoId: string;
  className?: string;
  onError?: () => void;
}

/**
 * YouTubeEmbed component for displaying YouTube videos as background media
 * 
 * Features:
 * - Responsive 16:9 aspect ratio
 * - Autoplay, muted, and looped playback
 * - No controls for ambient background effect
 * - Gradient overlay for text readability
 * - Rounded corners matching design system
 * - Comprehensive error handling with fallback
 * - Load timeout detection
 * - Lazy loading with Intersection Observer
 * - Performance optimized with facade pattern
 * 
 * @param videoId - YouTube video ID (11 characters)
 * @param className - Optional additional CSS classes
 * @param onError - Optional callback when video fails to load
 */
export function YouTubeEmbed({ videoId, className = '', onError }: YouTubeEmbedProps) {
  const [hasError, setHasError] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [shouldLoad, setShouldLoad] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const timeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);

  // Construct YouTube embed URL with optimal parameters for background video
  // autoplay=1: Start playing automatically
  // mute=1: Mute audio (required for autoplay in most browsers)
  // loop=1: Loop the video continuously
  // playlist={videoId}: Required for loop to work
  // controls=0: Hide video controls
  // showinfo=0: Hide video title and uploader info (deprecated but still works)
  // rel=0: Don't show related videos at the end
  // modestbranding=1: Minimize YouTube branding
  // playsinline=1: Play inline on mobile devices
  const embedUrl = `https://www.youtube.com/embed/${videoId}?autoplay=1&mute=1&loop=1&playlist=${videoId}&controls=0&showinfo=0&rel=0&modestbranding=1&playsinline=1`;

  const handleError = (reason: string) => {
    console.error(`Failed to load YouTube video: ${videoId}`, {
      reason,
      timestamp: new Date().toISOString(),
      videoId,
    });
    
    setHasError(true);
    setIsLoading(false);
    
    // Call parent error handler if provided
    if (onError) {
      onError();
    }
  };

  // Lazy load iframe using Intersection Observer
  useEffect(() => {
    if (!containerRef.current) return;

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && !shouldLoad) {
            setShouldLoad(true);
            observer.disconnect();
          }
        });
      },
      {
        rootMargin: '50px', // Start loading 50px before entering viewport
        threshold: 0.1,
      }
    );

    observer.observe(containerRef.current);

    return () => {
      observer.disconnect();
    };
  }, [shouldLoad]);

  useEffect(() => {
    if (!shouldLoad) return;

    // Set a timeout to detect if the video fails to load
    // YouTube iframes don't always trigger onError, so we need a timeout
    timeoutRef.current = setTimeout(() => {
      if (isLoading) {
        console.error(`YouTube video load timeout: ${videoId} - Video may be unavailable or restricted`);
        handleError('Load timeout - video may be unavailable');
      }
    }, 10000); // 10 second timeout

    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [videoId, isLoading, shouldLoad]);

  const handleIframeLoad = () => {
    // Clear timeout when iframe loads successfully
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }
    setIsLoading(false);
    console.log(`YouTube video loaded successfully: ${videoId}`);
  };

  const handleIframeError = () => {
    handleError('Iframe error event');
  };

  // If there's an error loading the video, return null
  // The parent component should handle fallback to default image
  if (hasError) {
    console.log(`YouTubeEmbed returning null due to error, parent should show fallback`);
    return null;
  }

  return (
    <div ref={containerRef} className={`relative overflow-hidden rounded-2xl sm:rounded-3xl ${className}`}>
      {/* Responsive 16:9 aspect ratio container */}
      <div className="relative pb-[56.25%]">
        {shouldLoad ? (
          <iframe
            ref={iframeRef}
            src={embedUrl}
            className="absolute inset-0 h-full w-full"
            allow="autoplay; encrypted-media"
            allowFullScreen={false}
            onLoad={handleIframeLoad}
            onError={handleIframeError}
            title="Hero background video"
            loading="lazy"
            style={{
              border: 'none',
              pointerEvents: 'none', // Prevent interaction with video
            }}
          />
        ) : (
          // Placeholder before lazy load
          <div className="absolute inset-0 flex items-center justify-center bg-slate-200 dark:bg-slate-800 rounded-2xl sm:rounded-3xl transition-colors duration-300">
            <div className="text-slate-400 dark:text-slate-600">
              <svg className="h-8 w-8 sm:h-10 md:h-12 sm:w-10 md:w-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
        )}
      </div>
      
      {/* Gradient overlay for text readability - enhanced for dark mode */}
      <div 
        className="absolute inset-0 rounded-2xl sm:rounded-3xl bg-gradient-to-tr from-blue-600/10 via-blue-500/5 dark:from-blue-500/20 dark:via-blue-600/10 to-transparent pointer-events-none transition-colors duration-300"
        aria-hidden="true"
      />
      
      {/* Loading indicator - responsive sizing */}
      {shouldLoad && isLoading && (
        <div className="absolute inset-0 flex items-center justify-center bg-slate-200 dark:bg-slate-800 rounded-2xl sm:rounded-3xl transition-colors duration-300">
          <div className="animate-pulse text-slate-400 dark:text-slate-600">
            <svg className="h-8 w-8 sm:h-10 md:h-12 sm:w-10 md:w-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        </div>
      )}
    </div>
  );
}
