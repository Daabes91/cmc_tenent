'use client';

import { useEffect, useMemo, useState } from "react";
import { CheckCircle, Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { API_BASE_URL, PATIENT_WEB_URL, SALES_EMAIL } from "@/lib/constants";
import { useLanguage } from "@/contexts/LanguageContext";
import { healthcareCopy } from "@/lib/content/healthcare-copy";
import { PricingTier } from "@/lib/content/types";
import { useAnalytics } from "@/hooks/use-analytics";
import SectionCTA from './SectionCTA';

type RemotePlan = {
  tier: string;
  name: string;
  description: string;
  monthlyPrice: number | null;
  annualPrice: number | null;
  currency: string;
  features: string[];
  monthlyPlanId?: string | null;
  annualPlanId?: string | null;
  popular?: boolean;
};

type DisplayPlan = {
  tier: string;
  name: string;
  priceLabel: string;
  annualPriceLabel?: string;
  billingLabel: string;
  description: string;
  features: string[];
  limits: {
    providers?: number;
    patients?: number;
    appointments?: number;
  };
  cta: string;
  ctaHref: string;
  popular?: boolean;
};

// Helper function to convert healthcare pricing tiers to display plans
function convertHealthcareTierToDisplayPlan(tier: PricingTier, language: 'en' | 'ar'): DisplayPlan {
  const monthlyPrice = tier.price.monthly;
  const annualPrice = tier.price.annual;
  
  // Format monthly price
  const priceLabel = monthlyPrice !== null 
    ? formatPrice(monthlyPrice, 'USD', language === 'ar' ? 'ar-AE' : 'en-US') || `$${monthlyPrice}`
    : (language === 'ar' ? 'مخصص' : 'Custom');
  
  // Format annual price
  const annualPriceLabel = annualPrice !== null
    ? formatPrice(annualPrice, 'USD', language === 'ar' ? 'ar-AE' : 'en-US') || `$${annualPrice}`
    : undefined;
  
  // Determine CTA href
  const isEnterprise = tier.name.toLowerCase().includes('multi-location') || 
                       tier.name.toLowerCase().includes('enterprise') ||
                       tier.name.toLowerCase().includes('مؤسسات') ||
                       tier.name.toLowerCase().includes('متعددة المواقع');
  
  const ctaHref = isEnterprise
    ? `mailto:${SALES_EMAIL}?subject=${encodeURIComponent(tier.name + ' Plan Inquiry')}`
    : `/signup?plan=${encodeURIComponent(tier.name)}`;
  
  return {
    tier: tier.name.toUpperCase().replace(/\s+/g, '_'),
    name: tier.name,
    priceLabel,
    annualPriceLabel,
    billingLabel: language === 'ar' ? 'شهرياً' : 'per month',
    description: tier.description,
    features: tier.features,
    limits: tier.limits,
    cta: tier.cta,
    ctaHref,
    popular: tier.popular
  };
}

const copy = {
  en: {
    contactCta: "Contact sales",
    startTrial: "Start free trial",
    bookDemo: "Book a demo",
    monthLabel: "per month",
    annualLabel: "per year",
    annualSavings: "Save with annual billing",
    providers: "providers",
    patients: "patients",
    appointments: "appointments",
    unlimited: "Unlimited",
    comparisonTitle: "Compare Plans",
    comparisonSubtitle: "Choose the right plan for your clinic size and needs"
  },
  ar: {
    contactCta: "تواصل مع فريق المبيعات",
    startTrial: "ابدأ الفترة التجريبية",
    bookDemo: "احجز عرضاً توضيحياً",
    monthLabel: "شهرياً",
    annualLabel: "سنوياً",
    annualSavings: "وفر مع الفوترة السنوية",
    providers: "مقدمي خدمة",
    patients: "مرضى",
    appointments: "مواعيد",
    unlimited: "غير محدود",
    comparisonTitle: "قارن الخطط",
    comparisonSubtitle: "اختر الخطة المناسبة لحجم عيادتك واحتياجاتك"
  },
};

function formatPrice(value: number | null | undefined, currency: string, locale: string) {
  if (value === null || value === undefined) return null;
  try {
    return new Intl.NumberFormat(locale, {
      style: "currency",
      currency: currency || "USD",
      maximumFractionDigits: 0,
    }).format(value);
  } catch {
    return `${currency || "USD"} ${value}`;
  }
}

export default function Pricing() {
  const { language } = useLanguage();
  const [remotePlans, setRemotePlans] = useState<RemotePlan[]>([]);
  const [loading, setLoading] = useState(false);
  
  // Analytics tracking
  const { trackCTA } = useAnalytics();

  useEffect(() => {
    let isMounted = true;
    const fetchPlans = async () => {
      setLoading(true);
      try {
        const res = await fetch(`${API_BASE_URL}/api/public/plans?currency=USD`, { cache: "no-store" });
        if (!res.ok) throw new Error(`Failed to load plans: ${res.status}`);
        const data = await res.json();
        if (Array.isArray(data)) {
          if (isMounted) setRemotePlans(data as RemotePlan[]);
        } else if (Array.isArray(data?.data)) {
          if (isMounted) setRemotePlans(data.data as RemotePlan[]);
        }
      } catch (error) {
        console.error("Failed to fetch plans for pricing section", error);
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchPlans();
    return () => {
      isMounted = false;
    };
  }, []);

  // Get healthcare pricing tiers from content configuration
  const healthcareTiers = healthcareCopy[language].pricing.tiers;
  
  const plans: DisplayPlan[] = useMemo(() => {
    // Use healthcare content as the primary source
    return healthcareTiers.map(tier => convertHealthcareTierToDisplayPlan(tier, language));
  }, [language, healthcareTiers]);

  const pricingContent = healthcareCopy[language].pricing;
  const labels = copy[language];

  // Helper to format limit values
  const formatLimit = (value: number | undefined) => {
    if (value === undefined) return '';
    if (value === -1) return labels.unlimited;
    return value.toString();
  };

  return (
    <section id="pricing" className="relative py-20 md:py-32">
      <div className="absolute inset-0 opacity-5">
        <svg width="100%" height="100%">
          <pattern id="pattern-circles" x="0" y="0" width="50" height="50" patternUnits="userSpaceOnUse" patternContentUnits="userSpaceOnUse">
            <circle id="pattern-circle" cx="10" cy="10" r="1.6257413380501518" fill="#fff"></circle>
          </pattern>
          <rect id="rect" x="0" y="0" width="100%" height="100%" fill="url(#pattern-circles)"></rect>
        </svg>
      </div>

      <div className="container relative px-4 md:px-8">
        {/* Pricing Header */}
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="mb-4 text-3xl font-bold tracking-tight md:text-4xl text-slate-900 dark:text-white">
            {pricingContent.title}
          </h2>
          <p className="mb-16 text-lg text-slate-600 dark:text-gray-400">{pricingContent.subtitle}</p>
        </div>

        {/* Pricing Cards */}
        <div className="grid gap-8 md:grid-cols-3 mb-20">
          {plans.map((plan, index) => (
            <div
              key={`${plan.tier}-${index}`}
              className={`flex flex-col rounded-xl border bg-white/90 dark:bg-gray-900/50 p-8 backdrop-blur-sm ${
                plan.popular ? "border-2 border-primary relative" : "border-slate-200 dark:border-gray-800"
              }`}
            >
              {plan.popular && (
                <div className="absolute -top-4 left-1/2 -translate-x-1/2 rounded-full bg-mintlify-gradient px-4 py-1 text-sm font-medium text-white">
                  {language === "ar" ? "الخيار المفضل" : "Most Popular"}
                </div>
              )}
              
              {/* Plan Header */}
              <div className="mb-6">
                <h3 className="mb-2 text-2xl font-bold text-slate-900 dark:text-white">{plan.name}</h3>
                <p className="text-slate-600 dark:text-gray-400 mb-4">{plan.description}</p>
                
                {/* Pricing */}
                <div className="mb-2">
                  <div className="flex items-baseline gap-2">
                    <span className="text-4xl font-bold text-slate-900 dark:text-white">{plan.priceLabel}</span>
                    <span className="text-slate-600 dark:text-gray-400 text-sm">/{labels.monthLabel}</span>
                  </div>
                  {plan.annualPriceLabel && (
                    <div className="mt-2 text-sm text-slate-600 dark:text-gray-400">
                      {plan.annualPriceLabel} /{labels.annualLabel} <span className="text-primary">({labels.annualSavings})</span>
                    </div>
                  )}
                </div>
                
                {/* Healthcare-specific limits */}
                {(plan.limits.providers !== undefined || plan.limits.patients !== undefined || plan.limits.appointments !== undefined) && (
                  <div className="mt-4 pt-4 border-t border-slate-200 dark:border-gray-700">
                    <div className="grid grid-cols-3 gap-2 text-center text-sm">
                      {plan.limits.providers !== undefined && (
                        <div>
                          <div className="font-semibold text-slate-900 dark:text-white">{formatLimit(plan.limits.providers)}</div>
                          <div className="text-slate-600 dark:text-gray-400 text-xs">{labels.providers}</div>
                        </div>
                      )}
                      {plan.limits.patients !== undefined && (
                        <div>
                          <div className="font-semibold text-slate-900 dark:text-white">{formatLimit(plan.limits.patients)}</div>
                          <div className="text-slate-600 dark:text-gray-400 text-xs">{labels.patients}</div>
                        </div>
                      )}
                      {plan.limits.appointments !== undefined && (
                        <div>
                          <div className="font-semibold text-slate-900 dark:text-white">{formatLimit(plan.limits.appointments)}</div>
                          <div className="text-slate-600 dark:text-gray-400 text-xs">{labels.appointments}</div>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>

              {/* Features List */}
              <ul className="mb-8 flex flex-col gap-3 flex-grow">
                {plan.features.map((feature, featureIndex) => (
                  <li key={featureIndex} className="flex items-start gap-2">
                    <CheckCircle className="h-5 w-5 text-primary/80 flex-shrink-0 mt-0.5" />
                    <span className="text-slate-700 dark:text-gray-300 text-sm">{feature}</span>
                  </li>
                ))}
              </ul>

              {/* CTA Button */}
              <Button
                className={`mt-auto ${
                  plan.popular
                    ? "bg-mintlify-gradient text-white hover:opacity-90"
                    : "bg-slate-900 text-white hover:bg-slate-800 dark:bg-gray-800 dark:hover:bg-gray-700"
                }`}
                asChild
              >
                <a 
                  href={plan.ctaHref} 
                  target={plan.ctaHref.startsWith("http") ? "_blank" : undefined} 
                  rel="noreferrer"
                  onClick={() => trackCTA(plan.cta, `pricing_${plan.tier}`)}
                >
                  {plan.cta}
                </a>
              </Button>
            </div>
          ))}
        </div>

        {/* Comparison Table */}
        <div className="mx-auto max-w-5xl">
          <div className="text-center mb-8">
            <h3 className="text-2xl font-bold text-slate-900 dark:text-white mb-2">
              {labels.comparisonTitle}
            </h3>
            <p className="text-slate-600 dark:text-gray-400">
              {labels.comparisonSubtitle}
            </p>
          </div>
          
          <div className="overflow-x-auto">
            <table className="w-full border-collapse bg-white/90 dark:bg-gray-900/50 rounded-xl overflow-hidden backdrop-blur-sm">
              <thead>
                <tr className="border-b border-slate-200 dark:border-gray-700">
                  <th className="text-left p-4 font-semibold text-slate-900 dark:text-white">
                    {language === 'ar' ? 'الميزة' : 'Feature'}
                  </th>
                  {plans.map((plan, index) => (
                    <th key={index} className="text-center p-4 font-semibold text-slate-900 dark:text-white">
                      {plan.name}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {/* Limits comparison */}
                <tr className="border-b border-slate-200 dark:border-gray-700">
                  <td className="p-4 text-slate-700 dark:text-gray-300">{labels.providers}</td>
                  {plans.map((plan, index) => (
                    <td key={index} className="text-center p-4 text-slate-700 dark:text-gray-300">
                      {formatLimit(plan.limits.providers)}
                    </td>
                  ))}
                </tr>
                <tr className="border-b border-slate-200 dark:border-gray-700">
                  <td className="p-4 text-slate-700 dark:text-gray-300">{labels.patients}</td>
                  {plans.map((plan, index) => (
                    <td key={index} className="text-center p-4 text-slate-700 dark:text-gray-300">
                      {formatLimit(plan.limits.patients)}
                    </td>
                  ))}
                </tr>
                <tr className="border-b border-slate-200 dark:border-gray-700">
                  <td className="p-4 text-slate-700 dark:text-gray-300">{labels.appointments}</td>
                  {plans.map((plan, index) => (
                    <td key={index} className="text-center p-4 text-slate-700 dark:text-gray-300">
                      {formatLimit(plan.limits.appointments)}
                    </td>
                  ))}
                </tr>
                
                {/* Feature comparison - show first 5 unique features across all plans */}
                {Array.from(new Set(plans.flatMap(p => p.features.slice(0, 5)))).slice(0, 5).map((feature, featureIndex) => (
                  <tr key={featureIndex} className="border-b border-slate-200 dark:border-gray-700">
                    <td className="p-4 text-slate-700 dark:text-gray-300 text-sm">{feature}</td>
                    {plans.map((plan, planIndex) => (
                      <td key={planIndex} className="text-center p-4">
                        {plan.features.includes(feature) ? (
                          <Check className="h-5 w-5 text-primary mx-auto" />
                        ) : (
                          <span className="text-slate-400 dark:text-gray-600">—</span>
                        )}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* CTA after pricing section */}
        <SectionCTA variant="pricing" className="mt-8" />
      </div>
    </section>
  );
}
