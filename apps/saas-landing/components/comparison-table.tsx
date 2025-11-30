import { Check, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import {PATIENT_WEB_URL, SALES_EMAIL} from '@/lib/constants';
import {useLanguage} from '@/contexts/LanguageContext';

const comparisonCopy = {
  en: {
    badge: 'Plan Comparison',
    title: 'Pick the runway your clinic needs',
    subtitle: 'Every tier shares the same backend and design system—you just turn on the governance and support you need.',
    launch: {
      name: 'Launch',
      price: '$149',
      note: '/mo/clinic',
      description: 'Independent clinics modernizing bookings',
      cta: 'Start trial',
      ctaHref: `${PATIENT_WEB_URL}?tenant=demo`,
    },
    growth: {
      name: 'Growth',
      price: '$349',
      note: '/mo/clinic',
      description: 'Multi-location groups scaling operations',
      cta: 'Book a demo',
      ctaHref: `mailto:${SALES_EMAIL}?subject=Growth%20Plan%20Demo`,
    },
    enterprise: {
      name: 'Enterprise',
      price: 'Custom',
      note: '',
      description: 'Hospital networks, governments, NGOs',
      cta: 'Contact sales',
      ctaHref: `mailto:${SALES_EMAIL}?subject=Enterprise%20Partnership`,
    },
    features: [
      { name: "Branded clinic website + blog", launch: true, growth: true, enterprise: true },
      { name: "Online appointments & reminders", launch: true, growth: true, enterprise: true },
      { name: "Virtual consultations & payments", launch: true, growth: true, enterprise: true },
      { name: "Smart admin workspace", launch: true, growth: true, enterprise: true },
      { name: "Multiple locations & shared branding", launch: false, growth: true, enterprise: true },
      { name: "Automation & marketing integrations", launch: false, growth: true, enterprise: true },
      { name: "White-glove onboarding", launch: false, growth: false, enterprise: true },
      { name: "SLA + custom integrations", launch: false, growth: false, enterprise: true },
    ],
  },
  ar: {
    badge: 'مقارنة الباقات',
    title: 'اختر الخطة الأنسب لمرحلة عيادتك',
    subtitle: 'البنية نفسها لكل الباقات، وما عليك سوى اختيار مستوى الدعم والخصائص التي تحتاجها.',
    launch: {
      name: 'انطلاقة',
      price: '$149',
      note: '/شهري لكل فرع',
      description: 'عيادات مستقلة تريد تحديث الحجوزات',
      cta: 'ابدأ التجربة',
      ctaHref: `${PATIENT_WEB_URL}?tenant=demo`,
    },
    growth: {
      name: 'نمو',
      price: '$349',
      note: '/شهري لكل فرع',
      description: 'شبكات متعددة الفروع تحتاج إلى الأتمتة',
      cta: 'احجز عرضاً',
      ctaHref: `mailto:${SALES_EMAIL}?subject=Growth%20Plan%20Demo`,
    },
    enterprise: {
      name: 'مؤسسات',
      price: 'مخصص',
      note: '',
      description: 'المستشفيات والهيئات الحكومية والمنظمات',
      cta: 'تواصل مع المبيعات',
      ctaHref: `mailto:${SALES_EMAIL}?subject=Enterprise%20Partnership`,
    },
    features: [
      { name: "موقع يحمل هوية عيادتك + مدونة", launch: true, growth: true, enterprise: true },
      { name: "حجوزات إلكترونية مع التذكيرات", launch: true, growth: true, enterprise: true },
      { name: "استشارات افتراضية ومدفوعات آمنة", launch: true, growth: true, enterprise: true },
      { name: "لوحة تحكم موحدة", launch: true, growth: true, enterprise: true },
      { name: "إدارة عدة فروع وهوية موحّدة", launch: false, growth: true, enterprise: true },
      { name: "تكاملات الأتمتة والتسويق", launch: false, growth: true, enterprise: true },
      { name: "إعداد ودعم مخصص", launch: false, growth: false, enterprise: true },
      { name: "اتفاقيات SLA وتكاملات خاصة", launch: false, growth: false, enterprise: true },
    ],
  },
} as const;

export default function ComparisonTable() {
  const {language} = useLanguage();
  const data = comparisonCopy[language];

  return (
    <section className="relative py-20 md:py-32 overflow-hidden">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-white to-slate-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950"></div>

      {/* Decorative elements */}
      <div className="absolute top-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-primary to-transparent opacity-30"></div>
      <div className="absolute bottom-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-primary to-transparent opacity-30"></div>

      <div className="container relative px-4 md:px-8">
        <div className="max-w-3xl mx-auto text-center mb-16">
          <p className="text-primary dark:text-primary/80 font-medium mb-2">{data.badge}</p>
          <h2 className="text-3xl md:text-4xl font-bold mb-6 text-slate-900 dark:text-white">{data.title}</h2>
          <p className="text-slate-600 dark:text-gray-400 text-lg">
            {data.subtitle}
          </p>
        </div>

        <div className="max-w-5xl mx-auto">
          <div className="overflow-x-auto">
            <div className="min-w-[800px]">
              {/* Table header */}
              <div className="grid grid-cols-4 gap-4 mb-8">
                <div className="col-span-1"></div>
                <div className="col-span-1 text-center">
                  <div className="font-bold text-xl mb-2 text-slate-900 dark:text-white">{data.launch.name}</div>
                  <div className="text-3xl font-bold mb-2 text-slate-900 dark:text-white">
                    {data.launch.price}
                    {data.launch.note && <span className="text-lg text-slate-600 dark:text-gray-400">{data.launch.note}</span>}
                  </div>
                  <div className="text-sm text-slate-600 dark:text-gray-400 mb-4">{data.launch.description}</div>
                  <Button variant="outline" className="w-full border-slate-300 hover:bg-slate-100 dark:border-gray-700 dark:hover:bg-gray-800" asChild>
                    <a href={data.launch.ctaHref} target="_blank" rel="noreferrer">
                      {data.launch.cta}
                    </a>
                  </Button>
                </div>
                <div className="col-span-1 text-center relative">
                  <div className="absolute -top-12 left-1/2 transform -translate-x-1/2 bg-mintlify-gradient text-white text-xs font-bold py-1 px-3 rounded-full">
                    {language === 'ar' ? 'الأكثر شيوعاً' : 'MOST POPULAR'}
                  </div>
                  <div className="font-bold text-xl mb-2 text-slate-900 dark:text-white">{data.growth.name}</div>
                  <div className="text-3xl font-bold mb-2 text-slate-900 dark:text-white">
                    {data.growth.price}
                    {data.growth.note && <span className="text-lg text-slate-600 dark:text-gray-400">{data.growth.note}</span>}
                  </div>
                  <div className="text-sm text-slate-600 dark:text-gray-400 mb-4">{data.growth.description}</div>
                  <Button className="w-full bg-mintlify-gradient hover:opacity-90" asChild>
                    <a href={data.growth.ctaHref}>{data.growth.cta}</a>
                  </Button>
                </div>
                <div className="col-span-1 text-center">
                  <div className="font-bold text-xl mb-2 text-slate-900 dark:text-white">{data.enterprise.name}</div>
                  <div className="text-3xl font-bold mb-2 text-slate-900 dark:text-white">
                    {data.enterprise.price}
                  </div>
                  <div className="text-sm text-slate-600 dark:text-gray-400 mb-4">{data.enterprise.description}</div>
                  <Button variant="outline" className="w-full border-slate-300 hover:bg-slate-100 dark:border-gray-700 dark:hover:bg-gray-800" asChild>
                    <a href={data.enterprise.ctaHref}>{data.enterprise.cta}</a>
                  </Button>
                </div>
              </div>

              {/* Table body */}
              <div className="space-y-4">
                {data.features.map((feature, index) => (
                  <div key={index} className="grid grid-cols-4 gap-4 py-4 border-t border-slate-200 dark:border-gray-800 text-slate-800 dark:text-white">
                    <div className="col-span-1 flex items-center font-medium">{feature.name}</div>
                    <div className="col-span-1 flex justify-center items-center">
                      {feature.launch ? (
                        <Check className="h-5 w-5 text-primary/80" />
                      ) : (
                        <X className="h-5 w-5 text-slate-400 dark:text-gray-600" />
                      )}
                    </div>
                    <div className="col-span-1 flex justify-center items-center">
                      {feature.growth ? (
                        <Check className="h-5 w-5 text-primary/80" />
                      ) : (
                        <X className="h-5 w-5 text-slate-400 dark:text-gray-600" />
                      )}
                    </div>
                    <div className="col-span-1 flex justify-center items-center">
                      {feature.enterprise ? (
                        <Check className="h-5 w-5 text-primary/80" />
                      ) : (
                        <X className="h-5 w-5 text-slate-400 dark:text-gray-600" />
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
