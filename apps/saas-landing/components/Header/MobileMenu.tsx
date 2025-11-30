'use client';

import Link from "next/link";
import { Button } from "@/components/ui/button";
import {
  CalendarClock,
  Users,
  BarChart3,
  FileText,
  BookOpen,
  MessageSquare,
  LayoutGrid,
  CreditCard,
  Star,
  LogIn,
  ChevronRight,
  Globe2,
  ShieldCheck,
  LifeBuoy,
} from "lucide-react";
import {SAAS_ADMIN_URL, PATIENT_WEB_URL, API_DOCS_URL, SALES_EMAIL} from '@/lib/constants';
import {ModeToggle} from '@/components/mode-toggle';
import {LanguageToggle} from '@/components/LanguageToggle';
import {useLanguage} from '@/contexts/LanguageContext';

const COPY = {
  en: {
    platformTitle: 'Platform',
    resourcesTitle: 'Resources',
    platform: {
      web: {label: 'Patient web', desc: 'Bilingual landing pages & bookings'},
      workspace: {label: 'Staff workspace', desc: 'Roles, schedules, care plans'},
      analytics: {label: 'Revenue analytics', desc: 'Payouts, inventory, retention'},
    },
    resources: {
      quickstart: {label: 'Launch checklist', desc: 'Everything you need to go live'},
      api: {label: 'Feature overview', desc: 'Dive deeper into every module'},
      playbooks: {label: 'Clinic playbooks', desc: 'Case studies & best practices'},
      help: {label: 'Implementation help', desc: 'Schedule a white-glove onboarding'},
    },
    nav: {
      features: 'Features',
      pricing: 'Pricing',
      security: 'Security',
      testimonials: 'Testimonials',
      login: 'Log in',
    },
    buttons: {
      demo: 'Launch patient demo',
      contact: 'Talk to our team',
    },
    appearance: 'Appearance',
  },
  ar: {
    platformTitle: 'المنصة',
    resourcesTitle: 'الموارد',
    platform: {
      web: {label: 'موقع المرضى', desc: 'صفحات هبوط ثنائية اللغة وحجوزات سهلة'},
      workspace: {label: 'لوحة الموظفين', desc: 'الأدوار والجداول والخطط العلاجية'},
      analytics: {label: 'تحليلات الإيرادات', desc: 'المدفوعات والمخزون ونسب الحضور'},
    },
    resources: {
      quickstart: {label: 'دليل الإطلاق', desc: 'كل ما تحتاجه للانطلاق'},
      api: {label: 'لمحة عن المزايا', desc: 'تعرّف على كل وحدة بالتفصيل'},
      playbooks: {label: 'الأدلة والقصص', desc: 'دراسات حالة وأفضل الممارسات'},
      help: {label: 'مساعدة التنفيذ', desc: 'احجز جلسة إعداد مخصّصة'},
    },
    nav: {
      features: 'الميزات',
      pricing: 'الأسعار',
      security: 'الأمان',
      testimonials: 'آراء العملاء',
      login: 'تسجيل الدخول',
    },
    buttons: {
      demo: 'جرّب النسخة المباشرة',
      contact: 'تحدث معنا',
    },
    appearance: 'المظهر',
  },
} as const;

export default function MobileMenu({
  isMenuOpen,
  setIsMenuOpen,
}: {
  isMenuOpen: boolean;
  setIsMenuOpen: (v: boolean) => void;
}) {
  const {language} = useLanguage();
  const text = COPY[language];
  if (!isMenuOpen) return null;
  
  return (
    <>
      {/* Backdrop overlay */}
      <div 
        className="md:hidden fixed inset-0 top-16 z-40 bg-black/60 backdrop-blur-sm animate-in fade-in duration-200"
        onClick={() => setIsMenuOpen(false)}
      />
      
      {/* Menu container - max-content height */}
      <div 
        className="md:hidden fixed top-16 left-0 right-0 z-50 animate-in slide-in-from-top duration-300 max-h-[85vh] overflow-y-auto"
      >
        <div 
          className="mx-3 mt-2 rounded-xl border border-slate-200 bg-gradient-to-b from-white to-slate-100 text-slate-900 shadow-lg dark:border-mintlify-mintDark/30 dark:from-[#1a1a2e] dark:to-[#16161e] dark:text-white"
        >
          <nav className="flex flex-col p-3">
            {/* Platform section */}
            <div className="mb-3 pb-3 border-b border-slate-200 dark:border-gray-800/50">
              <div className="flex items-center mb-2 px-1">
                <div className="w-1 h-4 bg-mintlify-gradient rounded-full mr-2"></div>
                <h3 className="font-semibold text-slate-900 dark:text-white text-base">{text.platformTitle}</h3>
              </div>
              <div className="space-y-0.5">
                <Link
                  href={`${PATIENT_WEB_URL}?tenant=demo`}
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <Globe2 className="h-4 w-4 text-primary/80 mr-2 group-hover:text-primary transition-colors" />
                  <div>
                    <div className="font-medium text-sm">{text.platform.web.label}</div>
                    <div className="text-xs text-slate-500 dark:text-gray-400 group-hover:text-gray-300 transition-colors">{text.platform.web.desc}</div>
                  </div>
                  <ChevronRight className="ml-auto h-3 w-3 text-gray-500 group-hover:text-gray-300 transition-colors" />
                </Link>
                <Link
                  href={SAAS_ADMIN_URL}
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <Users className="h-4 w-4 text-primary/80 mr-2 group-hover:text-primary transition-colors" />
                  <div>
                    <div className="font-medium text-sm">{text.platform.workspace.label}</div>
                    <div className="text-xs text-slate-500 dark:text-gray-400 group-hover:text-gray-300 transition-colors">{text.platform.workspace.desc}</div>
                  </div>
                  <ChevronRight className="ml-auto h-3 w-3 text-gray-500 group-hover:text-gray-300 transition-colors" />
                </Link>
                <Link
                  href="#pricing"
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <BarChart3 className="h-4 w-4 text-primary/80 mr-2 group-hover:text-primary transition-colors" />
                  <div>
                    <div className="font-medium text-sm">{text.platform.analytics.label}</div>
                    <div className="text-xs text-slate-500 dark:text-gray-400 group-hover:text-gray-300 transition-colors">{text.platform.analytics.desc}</div>
                  </div>
                  <ChevronRight className="ml-auto h-3 w-3 text-gray-500 group-hover:text-gray-300 transition-colors" />
                </Link>
              </div>
            </div>
            
            {/* Resources section */}
            <div className="mb-3 pb-3 border-b border-slate-200 dark:border-gray-800/50">
              <div className="flex items-center mb-2 px-1">
                <div className="w-1 h-4 bg-mintlify-gradient rounded-full mr-2"></div>
                <h3 className="font-semibold text-slate-900 dark:text-white text-base">{text.resourcesTitle}</h3>
              </div>
              <div className="space-y-0.5">
                <Link
                  href="https://github.com/MohamedDjoudir/landing-page-template-2/blob/main/QUICKSTART.md"
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <FileText className="h-4 w-4 text-mintlify-blue/80 mr-2 group-hover:text-mintlify-blue transition-colors" />
                  <div>
                    <div className="font-medium text-sm">{text.resources.quickstart.label}</div>
                    <div className="text-xs text-slate-500 dark:text-gray-400 group-hover:text-gray-300 transition-colors">{text.resources.quickstart.desc}</div>
                  </div>
                  <ChevronRight className="ml-auto h-3 w-3 text-gray-500 group-hover:text-gray-300 transition-colors" />
                </Link>
                <Link
                  href={API_DOCS_URL}
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <BookOpen className="h-4 w-4 text-mintlify-blue/80 mr-2 group-hover:text-mintlify-blue transition-colors" />
                  <div>
                    <div className="font-medium text-sm">{text.resources.api.label}</div>
                    <div className="text-xs text-slate-500 dark:text-gray-400 group-hover:text-gray-300 transition-colors">{text.resources.api.desc}</div>
                  </div>
                  <ChevronRight className="ml-auto h-3 w-3 text-gray-500 group-hover:text-gray-300 transition-colors" />
                </Link>
                <Link
                  href="#blog"
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <MessageSquare className="h-4 w-4 text-mintlify-blue/80 mr-2 group-hover:text-mintlify-blue transition-colors" />
                  <div>
                    <div className="font-medium text-sm">{text.resources.playbooks.label}</div>
                    <div className="text-xs text-slate-500 dark:text-gray-400 group-hover:text-gray-300 transition-colors">{text.resources.playbooks.desc}</div>
                  </div>
                  <ChevronRight className="ml-auto h-3 w-3 text-gray-500 group-hover:text-gray-300 transition-colors" />
                </Link>
                <Link
                  href={`mailto:${SALES_EMAIL}`}
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <LifeBuoy className="h-4 w-4 text-mintlify-blue/80 mr-2 group-hover:text-mintlify-blue transition-colors" />
                  <div>
                    <div className="font-medium text-sm">{text.resources.help.label}</div>
                    <div className="text-xs text-slate-500 dark:text-gray-400 group-hover:text-gray-300 transition-colors">{text.resources.help.desc}</div>
                  </div>
                  <ChevronRight className="ml-auto h-3 w-3 text-gray-500 group-hover:text-gray-300 transition-colors" />
                </Link>
              </div>
            </div>
            
            {/* Main navigation links */}
            <div className="mb-3 pb-3 border-b border-gray-800/50">
              <div className="grid grid-cols-2 gap-1">
                <Link
                  href="#features"
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <LayoutGrid className="h-4 w-4 text-indigo-400 mr-2 group-hover:text-indigo-300 transition-colors" />
                  <span className="font-medium text-sm">{text.nav.features}</span>
                </Link>
                <Link
                  href="#pricing"
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <CreditCard className="h-4 w-4 text-indigo-400 mr-2 group-hover:text-indigo-300 transition-colors" />
                  <span className="font-medium text-sm">{text.nav.pricing}</span>
                </Link>
                <Link
                  href="#faq"
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <ShieldCheck className="h-4 w-4 text-indigo-400 mr-2 group-hover:text-indigo-300 transition-colors" />
                  <span className="font-medium text-sm">{text.nav.security}</span>
                </Link>
                <Link
                  href="#testimonials"
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <Star className="h-4 w-4 text-indigo-400 mr-2 group-hover:text-indigo-300 transition-colors" />
                  <span className="font-medium text-sm">{text.nav.testimonials}</span>
                </Link>
                <Link
                  href={SAAS_ADMIN_URL}
                  className="flex items-center py-2 px-3 text-slate-700 dark:text-gray-200 rounded-lg hover:bg-slate-100 dark:hover:bg-gray-800 transition-all duration-200 group"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <LogIn className="h-4 w-4 text-slate-500 dark:text-gray-400 mr-2 group-hover:text-gray-300 transition-colors" />
                  <span className="font-medium text-sm">{text.nav.login}</span>
                </Link>
              </div>
            </div>
            
            {/* Theme + Get Started */}
            <div className="flex items-center justify-between px-1 py-2">
              <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">{text.appearance}</span>
              <div className="flex items-center gap-2">
                <LanguageToggle />
                <ModeToggle />
              </div>
            </div>

            <div className="space-y-2">
              <Button
                className="w-full py-3 bg-mintlify-gradient text-white text-sm shadow-[0_15px_45px_rgba(24,226,153,0.25)] hover:opacity-90 transition-all duration-200 flex items-center justify-center gap-2 font-medium"
                asChild
                onClick={() => setIsMenuOpen(false)}
              >
                <Link href={PATIENT_WEB_URL} target="_blank" rel="noreferrer">
                  {text.buttons.demo}
                  <ChevronRight className="h-3 w-3" />
                </Link>
              </Button>
              <Button
                variant="outline"
                className="w-full py-3 border-slate-300 text-slate-700 text-sm hover:bg-slate-100 dark:border-gray-700 dark:text-gray-100 dark:hover:bg-gray-900/80 flex items-center justify-center gap-2 font-medium"
                asChild
                onClick={() => setIsMenuOpen(false)}
              >
                <a href={`mailto:${SALES_EMAIL}`}>
                  {text.buttons.contact}
                  <ChevronRight className="h-3 w-3" />
                </a>
              </Button>
            </div>
          </nav>
        </div>
      </div>
    </>
  );
}
