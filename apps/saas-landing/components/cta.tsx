'use client';

import { Button } from "@/components/ui/button"
import { ArrowRight } from "lucide-react"
import { SALES_EMAIL } from '@/lib/constants';
import { useLanguage } from '@/contexts/LanguageContext';
import { useAnalytics } from '@/hooks/use-analytics';

const copy = {
  en: {
    title: 'Take your clinic to the next level',
    subtitle: 'Launch a beautiful website, run a smart admin panel, and keep every patient touchpoint organized from day one.',
    button: 'Book a strategy call',
    bullets: [
      "Data migration and training included",
      "WCAG-compliant SAAS admin",
      "Backed by the same API powering our clinics",
    ],
  },
  ar: {
    title: 'ارتقِ بعيادتك إلى المستوى التالي',
    subtitle: 'أطلق موقعاً جميلاً، وأدر لوحة تحكم ذكية، وحافظ على تنظيم كل تفاعل مع المرضى منذ اللحظة الأولى.',
    button: 'احجز مكالمة استشارية',
    bullets: [
      "يشمل نقل البيانات والتدريب",
      "لوحة تحكم متوافقة مع معايير الوصول",
      "منصة موثوقة تعتمد عليها عياداتنا",
    ],
  },
} as const;

export default function Cta() {
  const { language } = useLanguage();
  const { trackCTA } = useAnalytics();
  const data = copy[language];
  const isRTL = language === 'ar';
  
  const handleCTAClick = () => {
    trackCTA(data.button, 'final_cta_section');
  };
  return (
    <section className="relative py-16 overflow-hidden">
      {/* Simple background */}
      <div className="absolute inset-0 bg-white dark:bg-gray-950"></div>

      <div className="container relative px-4 md:px-8">
        <div className="max-w-4xl mx-auto">
          <div className="bg-white/95 dark:bg-gray-900/90 border border-slate-200 dark:border-gray-800 rounded-xl p-8 md:p-10 relative overflow-hidden shadow-lg transition-colors">
            {/* Simplified decorative elements - just one gradient */}
            <div className="absolute -top-32 -right-32 w-64 h-64 bg-mintlify-gradient opacity-30 rounded-full blur-3xl"></div>
            
            {/* Gradient border on left instead of top for visual interest */}
            <div className="absolute left-0 top-0 bottom-0 w-1 bg-mintlify-gradient rounded-full"></div>

            <div className="relative flex flex-col md:flex-row items-center justify-between gap-8">
              <div className="md:max-w-xl">
                <h2 className="text-2xl md:text-3xl font-bold mb-3 text-slate-900 dark:text-white">
                  {data.title}
                </h2>
                <p className="text-slate-600 dark:text-gray-300">
                  {data.subtitle}
                </p>
              </div>
              <div>
                <Button className="h-12 px-6 rounded-lg font-medium bg-mintlify-gradient hover:opacity-90 transition-all duration-300 shadow-[0_15px_45px_rgba(24,226,153,0.25)]" asChild>
                  <a
                    href={`mailto:${SALES_EMAIL}?subject=${encodeURIComponent('Clinic Management Platform - Strategy Call')}`}
                    onClick={handleCTAClick}
                    aria-label={data.button}
                  >
                    {data.button}
                    <ArrowRight className={`${isRTL ? 'mr-2' : 'ml-2'} h-4 w-4 ${isRTL ? 'rotate-180' : ''}`} aria-hidden="true" />
                  </a>
                </Button>
              </div>
            </div>

            {/* Simplified features row */}
            <div className="flex flex-wrap justify-center md:justify-start gap-6 mt-8 pt-6 border-t border-slate-200 dark:border-gray-800">
              {data.bullets.map((item) => (
                <div className="flex items-center gap-2" key={item}>
                  <svg className="w-5 h-5 text-primary/80" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                  </svg>
                  <span className="text-slate-600 dark:text-gray-300 text-sm">{item}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
