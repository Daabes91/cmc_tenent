'use client';

import { useEffect, useState, useMemo, useCallback } from 'react';
import { useTheme } from 'next-themes';

export function usePayPalTheme() {
  const { theme, resolvedTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  const [themeTransition, setThemeTransition] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  // Get current theme state - memoized to prevent unnecessary re-renders
  const isDarkMode = useMemo(() => {
    if (!mounted) return false;
    return resolvedTheme === 'dark' || 
      (resolvedTheme === 'system' && typeof window !== 'undefined' && 
       window.matchMedia('(prefers-color-scheme: dark)').matches);
  }, [mounted, resolvedTheme]);

  // Handle theme transitions
  useEffect(() => {
    if (!mounted) return;

    setThemeTransition(true);
    const timer = setTimeout(() => setThemeTransition(false), 300);

    return () => clearTimeout(timer);
  }, [isDarkMode, mounted]);

  // PayPal button style configuration - memoized and stable
  const getPayPalButtonStyle = useCallback(() => ({
    layout: 'vertical' as const,
    color: isDarkMode ? 'white' as const : 'blue' as const,
    shape: 'rect' as const,
    label: 'pay' as const,
    height: 55,
    tagline: false,
    fundingicons: false,
  }), [isDarkMode]);

  // Theme-specific colors - memoized for performance
  const getThemeColors = useCallback(() => ({
    primary: isDarkMode ? '#8b5cf6' : '#6366f1',
    secondary: isDarkMode ? '#06b6d4' : '#0ea5e9',
    background: isDarkMode ? 'rgba(15, 23, 42, 0.95)' : 'rgba(255, 255, 255, 0.95)',
    overlay: isDarkMode ? 'rgba(0, 0, 0, 0.8)' : 'rgba(0, 0, 0, 0.5)',
    border: isDarkMode ? 'rgba(148, 163, 184, 0.2)' : 'rgba(203, 213, 225, 0.5)',
    text: isDarkMode ? '#f8fafc' : '#0f172a',
    textSecondary: isDarkMode ? '#cbd5e1' : '#64748b',
  }), [isDarkMode]);

  return {
    mounted,
    isDarkMode,
    themeTransition,
    theme: resolvedTheme,
    getPayPalButtonStyle,
    getThemeColors,
  };
}