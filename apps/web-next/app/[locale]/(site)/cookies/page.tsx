import type { Metadata } from 'next';
import { LegalPage } from '@/components/legal/LegalPage';
import { buildLocalizedMetadata } from '@/lib/seo';

type LocaleKey = 'en' | 'ar';
const SUPPORTED_LOCALES: LocaleKey[] = ['en', 'ar'];

const COOKIES_CONTENT: Record<LocaleKey, { title: string; intro: string; updated: string; sections: { title: string; body: string; items?: string[] }[] }> = {
  en: {
    title: 'Cookie & Tracking Notice',
    intro: 'We use minimal cookies to keep the site secure, remember preferences, and understand which pages patients find helpful.',
    updated: 'Last updated: March 1, 2025',
    sections: [
      {
        title: 'Essential Cookies',
        body: 'Required for authentication and localization so we can deliver the correct language and protect patient dashboards.',
        items: [
          'Session cookies that expire after you log out',
          'Locale cookies that remember your preferred language',
        ],
      },
      {
        title: 'Performance & Analytics',
        body: 'With your consent we gather anonymous analytics to see how features perform and where patients drop off.',
        items: [
          'Page view counts, load times, and navigation flows',
          'Aggregated data that never includes treatment records',
        ],
      },
      {
        title: 'Managing Preferences',
        body: 'You can block cookies in your browser, but certain experiences (for example, bookings) may not function correctly.',
        items: [
          'Update browser settings to reject or delete cookies',
          'Use our consent banner to disable optional analytics',
          'Contact us if you have questions about specific trackers',
        ],
      },
    ],
  },
  ar: {
    title: 'إشعار ملفات تعريف الارتباط والتتبع',
    intro: 'نستخدم عددًا محدودًا من ملفات تعريف الارتباط للحفاظ على أمان الموقع، وتذكر التفضيلات، وفهم الصفحات الأكثر فائدة للمرضى.',
    updated: 'آخر تحديث: 1 مارس 2025',
    sections: [
      {
        title: 'ملفات تعريف الارتباط الأساسية',
        body: 'ضرورية للمصادقة وتحديد اللغة بحيث يمكننا تقديم المحتوى الصحيح وحماية لوحات المرضى.',
        items: [
          'ملفات جلسة تنتهي صلاحيتها بعد تسجيل الخروج',
          'ملفات لغة تتذكر تفضيلك للواجهة',
        ],
      },
      {
        title: 'الأداء والتحليلات',
        body: 'بموافقتك نجمع تحليلات مجهولة لنعرف كيفية عمل الميزات وأين يتوقف المرضى.',
        items: [
          'عدّ الصفحات وأوقات التحميل وتدفقات التنقل',
          'بيانات مجمعة لا تتضمن السجلات العلاجية',
        ],
      },
      {
        title: 'إدارة التفضيلات',
        body: 'يمكنك حظر ملفات تعريف الارتباط من المتصفح، ولكن قد تتأثر بعض التجارب (مثل الحجز) في حال تعطيلها.',
        items: [
          'تحديث إعدادات المتصفح لرفض الملفات أو حذفها',
          'استخدام شريط الموافقة لتعطيل التحليلات الاختيارية',
          'التواصل معنا للاستفسار عن أي أداة تتبع محددة',
        ],
      },
    ],
  },
};

const resolveLocale = (locale: string): LocaleKey =>
  (SUPPORTED_LOCALES.includes(locale as LocaleKey) ? (locale as LocaleKey) : 'en');

type PageProps = {
  params: { locale: string };
};

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const locale = resolveLocale(params.locale);
  const content = COOKIES_CONTENT[locale];

  return buildLocalizedMetadata({
    locale: params.locale,
    path: '/cookies',
    title: content.title,
    description: content.intro,
    keywords: ['cookie policy', 'tracking notice'],
  });
}

export default function CookiesPage({ params }: PageProps) {
  const locale = resolveLocale(params.locale);
  const content = COOKIES_CONTENT[locale];

  return <LegalPage {...content} />;
}
