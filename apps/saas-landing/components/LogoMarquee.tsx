'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { supportsMutationObserver, prefersReducedMotion, supportsCSSAnimations } from '@/lib/browser-compat';

interface LogoMarqueeProps {
  logos: string[];
  speed?: number;
  className?: string;
}

export const LogoMarquee: React.FC<LogoMarqueeProps> = ({ 
  logos, 
  speed = 30,
  className = '' 
}) => {
  const [isPaused, setIsPaused] = useState(false);
  const [isRTL, setIsRTL] = useState(false);
  const [shouldAnimate, setShouldAnimate] = useState(true);

  // Check for RTL direction and animation support
  useEffect(() => {
    const checkRTL = () => {
      if (typeof document !== 'undefined') {
        setIsRTL(document.documentElement.dir === 'rtl');
      }
    };
    
    // Check if animations should be enabled
    const animationsSupported = supportsCSSAnimations();
    const reducedMotion = prefersReducedMotion();
    setShouldAnimate(animationsSupported && !reducedMotion);
    
    checkRTL();
    
    // Listen for direction changes if MutationObserver is supported
    if (supportsMutationObserver() && typeof document !== 'undefined') {
      const observer = new MutationObserver(checkRTL);
      observer.observe(document.documentElement, {
        attributes: true,
        attributeFilter: ['dir']
      });
      
      return () => observer.disconnect();
    }
  }, []);

  // Duplicate logos three times for seamless infinite loop
  const duplicatedLogos = [...logos, ...logos, ...logos];

  const handleFocus = () => {
    if (shouldAnimate) setIsPaused(true);
  };

  const handleBlur = () => {
    if (shouldAnimate) setIsPaused(false);
  };

  return (
    <div 
      className={`relative h-12 sm:h-14 md:h-16 w-full overflow-hidden ${className}`}
      onMouseEnter={() => shouldAnimate && setIsPaused(true)}
      onMouseLeave={() => shouldAnimate && setIsPaused(false)}
      onFocus={handleFocus}
      onBlur={handleBlur}
      role="region"
      aria-label="Company logos"
      tabIndex={0}
    >
      <div 
        className="flex gap-6 sm:gap-8 items-center"
        style={{
          animation: shouldAnimate ? `marquee ${speed}s linear infinite` : 'none',
          animationPlayState: shouldAnimate && isPaused ? 'paused' : 'running',
          animationDirection: shouldAnimate && isRTL ? 'reverse' : 'normal',
        }}
        role="list"
      >
        {duplicatedLogos.map((logo, index) => (
          <div
            key={`${logo}-${index}`}
            className="flex-shrink-0 h-6 sm:h-7 md:h-8 w-auto transition-opacity duration-300 hover:opacity-100"
            role="listitem"
          >
            <Image
              src={`/images/logos/${logo}.png`}
              alt={`${logo.replace(/-/g, ' ')} logo`}
              width={120}
              height={32}
              className="h-6 sm:h-7 md:h-8 w-auto object-contain grayscale opacity-60 hover:opacity-100 transition-opacity"
              sizes="(min-width: 768px) 160px, 120px"
              style={{ filter: 'grayscale(100%)' }}
            />
          </div>
        ))}
      </div>
    </div>
  );
};
