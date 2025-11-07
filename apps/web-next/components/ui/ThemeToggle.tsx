'use client';

import {Moon, Sun} from 'lucide-react';
import {useTheme} from 'next-themes';
import {useEffect, useState, useMemo} from 'react';

export function ThemeToggle() {
  const [mounted, setMounted] = useState(false);
  const {theme, resolvedTheme, setTheme} = useTheme();

  // useEffect only runs on the client, so now we can safely show the UI
  useEffect(() => {
    setMounted(true);
  }, []);

  const currentTheme = useMemo(() => {
    if (!mounted) {
      return undefined;
    }
    return resolvedTheme ?? theme ?? 'light';
  }, [mounted, resolvedTheme, theme]);

  const toggleTheme = () => {
    const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
    setTheme(nextTheme);
  };

  if (!mounted) {
    return (
      <button
        className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 transition-colors"
        aria-label="Toggle theme"
      >
        <Sun className="h-5 w-5" />
      </button>
    );
  }

  return (
    <button
      onClick={toggleTheme}
      className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300 transition-all duration-200"
      aria-label="Toggle theme"
      aria-pressed={currentTheme === 'dark'}
    >
      {currentTheme === 'dark' ? (
        <Sun className="h-5 w-5" />
      ) : (
        <Moon className="h-5 w-5" />
      )}
    </button>
  );
}
