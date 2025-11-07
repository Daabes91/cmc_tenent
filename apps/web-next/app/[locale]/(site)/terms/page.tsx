import type { Metadata } from 'next';
import { LegalPage } from '@/components/legal/LegalPage';
import { buildLocalizedMetadata } from '@/lib/seo';

type LocaleKey = 'en' | 'ar';
const SUPPORTED_LOCALES: LocaleKey[] = ['en', 'ar'];

const TERMS_CONTENT: Record<LocaleKey, { title: string; intro: string; updated: string; sections: { title: string; body: string; items?: string[] }[] }> = {
  en: {
    title: 'Terms of Service',
    intro: 'These terms describe how patients may use our website, virtual consultations, and booking tools.',
    updated: 'Last updated: March 1, 2025',
    sections: [
      {
        title: 'Using Our Services',
        body: 'You agree to provide accurate information when creating appointments or communicating with our team.',
        items: [
          'Do not share login credentials with other people',
          'Only book appointments you intend to attend',
          'Notify us promptly if you suspect unauthorized access',
        ],
      },
      {
        title: 'Virtual Care & Payments',
        body: 'Tele-dentistry sessions may require video, audio, and payment details. You are responsible for ensuring a private environment.',
        items: [
          'Follow the doctor’s instructions before and after sessions',
          'Pay any applicable fees at the time of booking unless otherwise agreed',
          'Use supported browsers and secure networks when joining virtual visits',
        ],
      },
      {
        title: 'Updates & Contact',
        body: 'We may update these terms as we enhance our platform. Continued use indicates acceptance of the latest version.',
        items: [
          'Review this page periodically for the newest revision',
          'Contact our clinic if you have questions about a clause',
          'Discontinue use if you do not agree with the changes',
        ],
      },
    ],
  },
  ar: {
    title: 'شروط الخدمة',
    intro: 'توضح هذه الشروط كيفية استخدام المرضى لموقعنا وخدمات الاستشارات الافتراضية وأدوات الحجز.',
    updated: 'آخر تحديث: 1 مارس 2025',
    sections: [
      {
        title: 'استخدام خدماتنا',
        body: 'أنت توافق على تقديم معلومات دقيقة عند إنشاء المواعيد أو التواصل مع فريقنا.',
        items: [
          'عدم مشاركة بيانات تسجيل الدخول مع الآخرين',
          'حجز المواعيد التي تنوي حضورها فقط',
          'إبلاغنا فورًا في حال الشك بحدوث دخول غير مصرح به',
        ],
      },
      {
        title: 'الرعاية الافتراضية والمدفوعات',
        body: 'قد تتطلب جلسات الطب عن بعد فيديو وصوتًا وتفاصيل دفع. تقع عليك مسؤولية ضمان خصوصية المكان.',
        items: [
          'اتباع تعليمات الطبيب قبل وبعد الجلسات',
          'دفع الرسوم المطبقة وقت الحجز ما لم يتم الاتفاق خلاف ذلك',
          'استخدام متصفحات مدعومة وشبكات آمنة عند الانضمام إلى الزيارات الافتراضية',
        ],
      },
      {
        title: 'التحديثات والتواصل',
        body: 'قد نقوم بتحديث هذه الشروط أثناء تطوير المنصة. استمرار الاستخدام يعني قبول الإصدار الأحدث.',
        items: [
          'راجع هذه الصفحة بشكل دوري للاطلاع على الإصدارات الجديدة',
          'تواصل مع العيادة إذا كانت لديك أسئلة حول أي بند',
          'توقف عن الاستخدام إذا لم توافق على التغييرات',
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
  const content = TERMS_CONTENT[locale];

  return buildLocalizedMetadata({
    locale: params.locale,
    path: '/terms',
    title: content.title,
    description: content.intro,
    keywords: ['terms of service', 'patient agreement'],
  });
}

export default function TermsPage({ params }: PageProps) {
  const locale = resolveLocale(params.locale);
  const content = TERMS_CONTENT[locale];

  return <LegalPage {...content} />;
}
