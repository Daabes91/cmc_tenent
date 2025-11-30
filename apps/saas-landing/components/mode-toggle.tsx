'use client';

import {useTheme} from 'next-themes';
import {useEffect, useState} from 'react';
import {Sun, Moon} from 'lucide-react';
import {Button} from '@/components/ui/button';

export function ModeToggle() {
  const {theme, setTheme} = useTheme();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const isDark = mounted ? theme === 'dark' : true;

  return (
    <Button
      variant="ghost"
      size="icon"
      aria-label="Toggle color theme"
      onClick={() => setTheme(isDark ? 'light' : 'dark')}
      className="text-slate-600 hover:bg-slate-100 dark:text-gray-200 dark:hover:bg-gray-900"
    >
      {isDark ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
    </Button>
  );
}
