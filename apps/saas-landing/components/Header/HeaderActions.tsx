import { Button } from "@/components/ui/button";
import { Menu, X } from "lucide-react";
import {SAAS_ADMIN_URL, PATIENT_WEB_URL, SALES_EMAIL , CLINIC_ADMIN_URL} from '@/lib/constants';
import {ModeToggle} from '@/components/mode-toggle';
import {LanguageToggle} from '@/components/LanguageToggle';
import {useLanguage} from '@/contexts/LanguageContext';

const COPY = {
  en: {
    login: 'Log in',
    demo: 'Launch demo',
    contact: 'Talk to us',
    getStarted: 'Get Started',
  },
  ar: {
    login: 'تسجيل الدخول',
    demo: 'جرّب النسخة المباشرة',
    contact: 'تحدث معنا',
    getStarted: 'ابدأ الآن',
  },
} as const;

export default function HeaderActions({
  isMenuOpen,
  setIsMenuOpen,
}: {
  isMenuOpen: boolean;
  setIsMenuOpen: (v: boolean) => void;
}) {
  const {language} = useLanguage();
  const text = COPY[language];

  return (
    <div className="flex items-center gap-3">
      <a
        href={CLINIC_ADMIN_URL}
        target="_blank"
        rel="noreferrer"
       className="hidden md:block text-sm font-medium text-slate-600 dark:text-gray-300 hover:text-slate-900 dark:hover:text-white px-2 py-1 rounded hover:bg-slate-100 dark:hover:bg-gray-900 transition"
      >
        {text.login}
      </a>
      <Button
        className="hidden md:flex bg-primary text-primary-foreground hover:bg-primary/90"
        asChild
      >
        <a href="/landing/signup">
          {text.getStarted}
        </a>
      </Button>
      <Button
        className="bg-mintlify-gradient text-white shadow-[0_12px_35px_rgba(24,226,153,0.25)] hover:opacity-90 transition"
        asChild
      >
        <a href={PATIENT_WEB_URL} target="_blank" rel="noreferrer">
          {text.demo}
        </a>
      </Button>
      <Button
        variant="ghost"
        className="hidden md:flex text-sm font-medium text-slate-600 dark:text-gray-300 hover:text-slate-900 dark:hover:text-white px-2 py-1 rounded hover:bg-slate-100 dark:hover:bg-gray-900 transition"
        asChild
      >
        <a href={`mailto:${SALES_EMAIL}`}>{text.contact}</a>
      </Button>
      <div className="hidden md:block">
        <LanguageToggle />
      </div>
      <div className="hidden md:block">
        <ModeToggle />
      </div>
      <Button
        variant="ghost"
        size="icon"
        className="md:hidden text-slate-600 dark:text-gray-300"
        onClick={() => setIsMenuOpen(!isMenuOpen)}
        aria-label="Toggle menu"
      >
        {isMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
      </Button>
    </div>
  );
}
