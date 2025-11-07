import type { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';
import { VirtualCheckoutClient } from '@/components/checkout/VirtualCheckoutClient';

type PageProps = {
  params: { locale: string };
  searchParams: Record<string, string | string[] | undefined>;
};

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const t = await getTranslations({ locale: params.locale, namespace: 'booking' });
  return {
    title: `${t('title')} | Pay`,
  };
}

const normalizeSearchParams = (searchParams: PageProps['searchParams']): Record<string, string> => {
  const entries: Record<string, string> = {};
  Object.entries(searchParams).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      if (value.length > 0) {
        entries[key] = value[0];
      }
    } else if (value) {
      entries[key] = value;
    }
  });
  return entries;
};

export default function VirtualCheckoutPage({ params, searchParams }: PageProps) {
  const normalized = normalizeSearchParams(searchParams);

  return (
    <div className="relative z-10 bg-gradient-to-br from-slate-50 via-white to-blue-50/20 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
      <div className="mx-auto max-w-6xl px-4 sm:px-6 py-12 lg:py-16">
        <div className="mb-8 text-center">
          <span className="inline-flex items-center gap-2 rounded-full bg-blue-100 dark:bg-blue-900/30 px-4 py-2 text-sm font-semibold text-blue-700 dark:text-blue-300">
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            {params.locale === 'ar' ? 'دفع الاستشارة الافتراضية' : 'Virtual consultation payment'}
          </span>
          <h1 className="mt-4 text-3xl font-bold text-slate-900 dark:text-slate-100 sm:text-4xl">
            {params.locale === 'ar' ? 'أكمل حجز استشارتك' : 'Complete your booking'}
          </h1>
          <p className="mt-2 text-slate-600 dark:text-slate-300">
            {params.locale === 'ar'
              ? 'راجع تفاصيل موعدك وادفع بأمان لإنهاء الحجز.'
              : 'Review your appointment details and pay securely to confirm your booking.'}
          </p>
        </div>

        <VirtualCheckoutClient locale={params.locale} searchParams={normalized} />
      </div>
    </div>
  );
}
