'use client';

import {createContext, useContext, useEffect, useState, ReactNode} from 'react';

type Language = 'en' | 'ar';

type LanguageContextValue = {
  language: Language;
  setLanguage: (lang: Language) => void;
};

const LanguageContext = createContext<LanguageContextValue | undefined>(undefined);

export function LanguageProvider({children}: {children: ReactNode}) {
  const [language, setLanguageState] = useState<Language>('en');

  useEffect(() => {
    const stored = typeof window !== 'undefined' ? (localStorage.getItem('lang') as Language | null) : null;
    const initial = stored === 'ar' || stored === 'en' ? stored : 'en';
    setLanguageState(initial);
    if (typeof document !== 'undefined') {
      document.documentElement.dir = initial === 'ar' ? 'rtl' : 'ltr';
    }
  }, []);

  const setLanguage = (lang: Language) => {
    setLanguageState(lang);
    if (typeof window !== 'undefined') {
      localStorage.setItem('lang', lang);
    }
    if (typeof document !== 'undefined') {
      document.documentElement.dir = lang === 'ar' ? 'rtl' : 'ltr';
    }
  };

  return <LanguageContext.Provider value={{language, setLanguage}}>{children}</LanguageContext.Provider>;
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within LanguageProvider');
  }
  return context;
}
