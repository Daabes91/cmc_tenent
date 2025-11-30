'use client';

import React, { useState, useEffect } from 'react';
import { supportsCSSAnimations, prefersReducedMotion } from '@/lib/browser-compat';

interface AnimatedBackgroundProps {
  className?: string;
}

export const AnimatedBackground: React.FC<AnimatedBackgroundProps> = ({ 
  className = '' 
}) => {
  const [shouldAnimate, setShouldAnimate] = useState(true);

  useEffect(() => {
    // Check if animations should be enabled
    const animationsSupported = supportsCSSAnimations();
    const reducedMotion = prefersReducedMotion();
    setShouldAnimate(animationsSupported && !reducedMotion);
  }, []);

  return (
    <div className={`absolute inset-0 z-0 h-full w-full overflow-hidden ${className}`} aria-hidden="true">
      {/* SVG Wave Pattern */}
      <svg 
        className="absolute h-full w-full" 
        viewBox="0 0 1593 910" 
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        preserveAspectRatio="xMidYMid slice"
        role="presentation"
        aria-label="Decorative background waves"
      >
        {/* Background wave - subtle */}
        <path
          d="M0 300 Q 398.25 200, 796.5 300 T 1593 300 L 1593 910 L 0 910 Z"
          fill="url(#wave-gradient-1)"
          opacity="0.1"
          className={shouldAnimate ? "animate-wave-slow" : ""}
        />
        
        {/* Middle wave */}
        <path
          d="M0 400 Q 398.25 300, 796.5 400 T 1593 400 L 1593 910 L 0 910 Z"
          fill="url(#wave-gradient-2)"
          opacity="0.15"
          className={shouldAnimate ? "animate-wave-medium" : ""}
        />
        
        {/* Front wave - most visible */}
        <path
          d="M0 500 Q 398.25 400, 796.5 500 T 1593 500 L 1593 910 L 0 910 Z"
          fill="url(#wave-gradient-3)"
          opacity="0.2"
          className={shouldAnimate ? "animate-wave-fast" : ""}
        />

        {/* Gradient Definitions */}
        <defs>
          <linearGradient id="wave-gradient-1" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#18e299" stopOpacity="0.35" />
            <stop offset="100%" stopColor="#578bfa" stopOpacity="0.3" />
          </linearGradient>
          
          <linearGradient id="wave-gradient-2" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#578bfa" stopOpacity="0.45" />
            <stop offset="100%" stopColor="#006fed" stopOpacity="0.4" />
          </linearGradient>
          
          <linearGradient id="wave-gradient-3" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#18e299" stopOpacity="0.55" />
            <stop offset="100%" stopColor="#578bfa" stopOpacity="0.5" />
          </linearGradient>
        </defs>
      </svg>

      {/* Gradient Overlay - fades waves at top and bottom */}
      <div className="absolute inset-0 bg-gradient-to-b from-background via-transparent to-background pointer-events-none" />

      {/* Floating Blur Effects - responsive sizing */}
      <div className={`absolute top-1/4 left-1/4 w-64 h-64 sm:w-80 sm:h-80 md:w-96 md:h-96 bg-mintlify-mint/15 rounded-full blur-2xl sm:blur-3xl ${shouldAnimate ? 'animate-float-slow' : ''}`} />
      <div className={`absolute top-1/3 right-1/4 w-56 h-56 sm:w-72 sm:h-72 md:w-80 md:h-80 bg-mintlify-blue/15 rounded-full blur-2xl sm:blur-3xl ${shouldAnimate ? 'animate-float-medium' : ''}`} />
      <div className={`absolute bottom-1/4 left-1/3 w-48 h-48 sm:w-64 sm:h-64 md:w-72 md:h-72 bg-primary/20 rounded-full blur-2xl sm:blur-3xl ${shouldAnimate ? 'animate-float-fast' : ''}`} />
    </div>
  );
};
