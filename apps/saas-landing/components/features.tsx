'use client';

import { Globe, CalendarCheck, Users, DollarSign, MonitorSmartphone, BarChart, CheckCircle } from "lucide-react"
import {useLanguage} from '@/contexts/LanguageContext';
import { healthcareCopy } from '@/lib/content/healthcare-copy';
import { useAnalytics } from '@/hooks/use-analytics';
import SectionCTA from './SectionCTA';

// Icon mapping for healthcare features
const iconMap: Record<string, React.ReactNode> = {
  Globe: <Globe className="h-6 w-6" />,
  CalendarCheck: <CalendarCheck className="h-6 w-6" />,
  Users: <Users className="h-6 w-6" />,
  DollarSign: <DollarSign className="h-6 w-6" />,
  MonitorSmartphone: <MonitorSmartphone className="h-6 w-6" />,
  BarChart: <BarChart className="h-6 w-6" />,
};

export default function Features() {
  const {language} = useLanguage();
  const data = healthcareCopy[language].features;
  const { trackFeature } = useAnalytics();
  
  // Map features to include icons
  const featuresWithIcons = data.items.map(item => ({
    ...item,
    icon: iconMap[item.icon] || <Globe className="h-6 w-6" />
  }));
  
  const highlights = featuresWithIcons.slice(0, 3);
  
  // Handle feature card click
  const handleFeatureClick = (featureName: string) => {
    trackFeature(featureName, 'click');
  };

  return (
    <section id="features" className="relative py-20 md:py-32 overflow-hidden">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-slate-50 via-white to-slate-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950"></div>
      <div className="pointer-events-none absolute -top-40 right-10 h-72 w-72 rounded-full bg-mintlify-mint/15 blur-3xl"></div>
      <div className="pointer-events-none absolute -bottom-32 left-0 h-96 w-80 rounded-full bg-mintlify-blue/15 blur-[180px]"></div>

      <div className="container relative px-4 md:px-8">
        <div className="grid gap-12 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.2fr)] items-start">
          <div className="space-y-8">
            <div className="space-y-4">
              <span className="inline-flex items-center gap-2 rounded-full bg-primary/5 px-4 py-1 text-xs font-semibold uppercase tracking-wide text-primary dark:bg-primary/15 dark:text-primary/80">
                {language === 'en' ? 'Use cases' : 'مجالات الاستخدام'}
              </span>
              <h2 className="text-3xl md:text-4xl font-bold text-slate-900 dark:text-white">
                {data.title}
              </h2>
              <p className="text-lg text-slate-600 dark:text-gray-400">
                {data.subtitle}
              </p>
            </div>

            <div className="grid gap-4">
              {highlights.map((feature) => (
                <div key={feature.title} className="flex items-start gap-3 rounded-xl border border-slate-200/70 bg-white/80 p-4 shadow-sm dark:border-gray-800 dark:bg-gray-900/60">
                  <CheckCircle className="h-5 w-5 text-primary mt-0.5" />
                  <div>
                    <p className="font-semibold text-slate-900 dark:text-white">{feature.title}</p>
                    <p className="text-sm text-slate-600 dark:text-gray-400">{feature.description}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="rounded-2xl border border-primary/20 bg-gradient-to-r from-primary/5 to-mintlify-blue/5 p-6 text-sm text-slate-600 dark:border-primary/30 dark:bg-gradient-to-r dark:from-primary/20 dark:to-mintlify-blue/20 dark:text-gray-200">
              {language === 'en'
                ? 'Every clinic gets its own pixel-perfect website, a smart admin panel, and the automation layer that keeps bookings, payments, and communications running smoothly.'
                : 'تحصل كل عيادة على موقع متكامل ولوحة تحكم ذكية وطبقة أتمتة تحافظ على الحجوزات والمدفوعات والتواصل بسلاسة.'}
            </div>
          </div>

          <div className="grid gap-6 sm:grid-cols-2">
            {featuresWithIcons.map((feature, index) => (
              <div
                key={feature.title}
                className={`relative overflow-hidden rounded-2xl border p-6 transition transform hover:-translate-y-1 cursor-pointer ${
                  index % 2 === 0
                    ? 'border-slate-200 bg-white shadow-xl dark:border-gray-800 dark:bg-gray-900/80'
                    : 'border-transparent bg-gradient-to-br from-primary/10 to-mintlify-blue/10 shadow-xl shadow-[0_20px_45px_rgba(24,226,153,0.15)] dark:from-primary/20 dark:to-mintlify-blue/20'
                }`}
                onClick={() => handleFeatureClick(feature.title)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    handleFeatureClick(feature.title);
                  }
                }}
              >
                <div className="absolute right-4 top-4 h-16 w-16 rounded-full bg-primary/15 blur-3xl"></div>
                <div className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-full bg-white shadow-md dark:bg-gray-800">
                  {feature.icon}
                </div>
                <h3 className="text-lg font-semibold text-slate-900 dark:text-white">{feature.title}</h3>
                <p className="mt-2 text-sm text-slate-600 dark:text-gray-400">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* CTA after features section */}
      <SectionCTA variant="features" />
    </section>
  )
}
