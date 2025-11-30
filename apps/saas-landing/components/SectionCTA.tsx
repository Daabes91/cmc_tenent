'use client';

import { Button } from "@/components/ui/button";
import { ArrowRight } from "lucide-react";
import { SALES_EMAIL } from '@/lib/constants';
import { useLanguage } from '@/contexts/LanguageContext';
import { useAnalytics } from '@/hooks/use-analytics';

interface SectionCTAProps {
  variant?: 'features' | 'testimonials' | 'pricing';
  className?: string;
}

const copy = {
  en: {
    features: {
      title: "Ready to streamline your clinic operations?",
      subtitle: "Join hundreds of healthcare providers who trust our platform",
      primaryCTA: "Start Free Trial",
      secondaryCTA: "Schedule a Demo"
    },
    testimonials: {
      title: "Transform your clinic today",
      subtitle: "See why healthcare professionals choose our platform for their practice management",
      primaryCTA: "Get Started Now",
      secondaryCTA: "Talk to Sales"
    },
    pricing: {
      title: "Still have questions?",
      subtitle: "Our team is here to help you choose the right plan for your clinic",
      primaryCTA: "Contact Sales",
      secondaryCTA: "View Documentation"
    }
  },
  ar: {
    features: {
      title: "هل أنت مستعد لتبسيط عمليات عيادتك؟",
      subtitle: "انضم إلى مئات مقدمي الرعاية الصحية الذين يثقون في منصتنا",
      primaryCTA: "ابدأ الفترة التجريبية المجانية",
      secondaryCTA: "احجز عرضاً توضيحياً"
    },
    testimonials: {
      title: "حوّل عيادتك اليوم",
      subtitle: "اكتشف لماذا يختار المتخصصون في الرعاية الصحية منصتنا لإدارة ممارساتهم",
      primaryCTA: "ابدأ الآن",
      secondaryCTA: "تحدث مع المبيعات"
    },
    pricing: {
      title: "لا تزال لديك أسئلة؟",
      subtitle: "فريقنا هنا لمساعدتك في اختيار الخطة المناسبة لعيادتك",
      primaryCTA: "اتصل بالمبيعات",
      secondaryCTA: "عرض الوثائق"
    }
  }
};

export default function SectionCTA({ variant = 'features', className = '' }: SectionCTAProps) {
  const { language } = useLanguage();
  const { trackCTA } = useAnalytics();
  const data = copy[language][variant];
  const isRTL = language === 'ar';

  // Determine CTA destinations based on variant
  const getPrimaryCTAHref = () => {
    if (variant === 'pricing') {
      return `mailto:${SALES_EMAIL}?subject=${encodeURIComponent('Clinic Management Platform Inquiry')}`;
    }
    return '/signup';
  };

  const getSecondaryCTAHref = () => {
    if (variant === 'pricing') {
      return '/docs'; // Documentation link
    }
    return `mailto:${SALES_EMAIL}?subject=${encodeURIComponent('Schedule a Demo')}`;
  };

  const handlePrimaryCTAClick = () => {
    trackCTA(data.primaryCTA, `${variant}_section_cta`);
  };

  const handleSecondaryCTAClick = () => {
    trackCTA(data.secondaryCTA, `${variant}_section_cta`);
  };

  return (
    <div className={`relative py-12 ${className}`}>
      <div className="container px-4 md:px-8">
        <div className="mx-auto max-w-3xl">
          <div className="relative overflow-hidden rounded-2xl border border-primary/20 bg-gradient-to-br from-primary/5 via-white to-mintlify-blue/5 dark:from-primary/10 dark:via-gray-900 dark:to-mintlify-blue/10 p-8 md:p-12 text-center shadow-lg">
            {/* Decorative gradient orbs */}
            <div className="pointer-events-none absolute -top-24 -right-24 h-48 w-48 rounded-full bg-primary/20 blur-3xl" aria-hidden="true"></div>
            <div className="pointer-events-none absolute -bottom-24 -left-24 h-48 w-48 rounded-full bg-mintlify-blue/20 blur-3xl" aria-hidden="true"></div>
            
            <div className="relative z-10">
              <h3 className="mb-3 text-2xl font-bold text-slate-900 dark:text-white md:text-3xl">
                {data.title}
              </h3>
              <p className="mb-8 text-base text-slate-600 dark:text-gray-400 md:text-lg">
                {data.subtitle}
              </p>
              
              <div className="flex flex-col gap-4 sm:flex-row sm:justify-center">
                <Button
                  size="lg"
                  className="h-12 bg-primary px-8 text-base font-semibold hover:bg-primary/90 focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:outline-none transition-all"
                  asChild
                >
                  <a
                    href={getPrimaryCTAHref()}
                    onClick={handlePrimaryCTAClick}
                    aria-label={data.primaryCTA}
                  >
                    {data.primaryCTA}
                    <ArrowRight className={`${isRTL ? 'mr-2' : 'ml-2'} h-5 w-5 ${isRTL ? 'rotate-180' : ''}`} aria-hidden="true" />
                  </a>
                </Button>
                <Button
                  size="lg"
                  variant="outline"
                  className="h-12 px-8 text-base font-semibold border-primary/30 hover:bg-primary/10 focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:outline-none transition-all"
                  asChild
                >
                  <a
                    href={getSecondaryCTAHref()}
                    onClick={handleSecondaryCTAClick}
                    aria-label={data.secondaryCTA}
                  >
                    {data.secondaryCTA}
                  </a>
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
