'use client';

import React, { useRef, useState, useEffect } from 'react';
import { supports3DTransforms, prefersReducedMotion, isMobileDevice } from '@/lib/browser-compat';

interface Card3DProps {
  children: React.ReactNode;
  className?: string;
}

export const Card3D: React.FC<Card3DProps> = ({ children, className = '' }) => {
  const [transform, setTransform] = useState('');
  const [shouldEnable3D, setShouldEnable3D] = useState(false);
  const cardRef = useRef<HTMLDivElement>(null);

  // Check if 3D effect should be enabled based on browser capabilities and user preferences
  useEffect(() => {
    const check3DSupport = () => {
      const is3DSupported = supports3DTransforms();
      const isReducedMotion = prefersReducedMotion();
      const isMobile = isMobileDevice(768);
      
      // Enable 3D only if supported, not on mobile, and user doesn't prefer reduced motion
      setShouldEnable3D(is3DSupported && !isMobile && !isReducedMotion);
    };
    
    check3DSupport();
    window.addEventListener('resize', check3DSupport);
    
    return () => window.removeEventListener('resize', check3DSupport);
  }, []);

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    // Only apply 3D effect if supported and enabled
    if (!shouldEnable3D || !cardRef.current) return;

    const rect = cardRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const centerX = rect.width / 2;
    const centerY = rect.height / 2;

    // Calculate rotation based on mouse position
    // Divide by 20 to make the effect subtle
    const rotateX = (y - centerY) / 20;
    const rotateY = (centerX - x) / 20;

    setTransform(
      `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.02, 1.02, 1.02)`
    );
  };

  const handleMouseLeave = () => {
    // Reset to default state with smooth transition
    if (shouldEnable3D) {
      setTransform('perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)');
    }
  };

  const handleFocus = () => {
    // Apply subtle scale on focus for keyboard users
    if (shouldEnable3D) {
      setTransform('perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1.02, 1.02, 1.02)');
    }
  };

  const handleBlur = () => {
    // Reset on blur
    if (shouldEnable3D) {
      setTransform('perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)');
    }
  };

  return (
    <div
      ref={cardRef}
      onMouseMove={handleMouseMove}
      onMouseLeave={handleMouseLeave}
      onFocus={handleFocus}
      onBlur={handleBlur}
      tabIndex={0}
      role="img"
      style={{
        transform: shouldEnable3D ? transform : undefined,
        transition: shouldEnable3D ? 'transform 0.5s cubic-bezier(0.23, 1, 0.32, 1), box-shadow 0.5s cubic-bezier(0.23, 1, 0.32, 1)' : undefined,
        transformStyle: shouldEnable3D ? 'preserve-3d' : undefined,
        willChange: shouldEnable3D ? 'transform' : undefined,
      }}
      className={`${className} focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2`}
    >
      {children}
    </div>
  );
};
