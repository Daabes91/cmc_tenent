'use client';

import {useLanguage} from '@/contexts/LanguageContext';

export function LanguageToggle() {
  const {language, setLanguage} = useLanguage();

  return (
    <div className="flex rounded-full border border-slate-200 bg-white p-1 text-xs font-semibold shadow-sm dark:border-gray-700 dark:bg-gray-900">
      {(['en', 'ar'] as const).map((lang) => (
        <button
          key={lang}
          type="button"
          onClick={() => setLanguage(lang)}
          className={`px-3 py-1 rounded-full transition-colors ${
            language === lang
              ? 'bg-primary text-white shadow-sm'
              : 'text-slate-600 hover:text-slate-900 dark:text-gray-300 dark:hover:text-white'
          }`}
        >
          {lang === 'en' ? 'EN' : 'ع'}
        </button>
      ))}
    </div>
  );
}
