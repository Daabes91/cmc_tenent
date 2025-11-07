'use client';

import { usePayPalTheme } from '@/hooks/usePayPalTheme';

export default function PayPalThemeNote() {
  const { mounted, isDarkMode } = usePayPalTheme();

  if (!mounted) return null;

  return (
    <div className={`rounded-lg p-4 border ${
      isDarkMode 
        ? 'bg-amber-900/20 border-amber-800 text-amber-200' 
        : 'bg-amber-50 border-amber-200 text-amber-800'
    }`}>
      <div className="flex items-start gap-3">
        <svg className="h-5 w-5 mt-0.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div className="space-y-2">
          <h4 className="font-semibold text-sm">PayPal Theme Integration</h4>
          <div className="text-xs space-y-1">
            <p>
              <strong>Current Theme:</strong> {isDarkMode ? 'Dark' : 'Light'} mode detected
            </p>
            <p>
              <strong>PayPal Button:</strong> Automatically adapts to your theme ({isDarkMode ? 'white' : 'blue'} style)
            </p>
            <p>
              <strong>PayPal Modal:</strong> We apply custom styling to match your theme, but PayPal's iframe content 
              is served from their servers and may not fully respect dark mode preferences.
            </p>
            <p>
              <strong>Workaround:</strong> Our implementation applies CSS filters and custom styling to improve 
              dark mode compatibility, though some elements may still appear in light theme.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}