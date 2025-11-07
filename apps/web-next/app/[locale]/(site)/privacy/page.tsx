import type { Metadata } from 'next';
import { LegalPage } from '@/components/legal/LegalPage';
import { buildLocalizedMetadata } from '@/lib/seo';

type LocaleKey = 'en' | 'ar';

const SUPPORTED_LOCALES: LocaleKey[] = ['en', 'ar'];

const PRIVACY_CONTENT: Record<LocaleKey, { title: string; intro: string; updated: string; sections: { title: string; body: string; items?: string[] }[] }> = {
  en: {
    title: 'Privacy Policy',
    intro: 'We respect your trust and explain here how patient information is collected, protected, and used across our digital experiences.',
    updated: 'Last updated: March 1, 2025',
    sections: [
      {
        title: 'Information We Collect',
        body: 'We only capture the data required to provide dental care, manage appointments, and personalize your experience.',
        items: [
          'Contact details such as name, email, and phone number',
          'Appointment preferences, selected services, and doctor notes',
          'Insurance or payment information shared during booking',
          'Website analytics that help us improve performance',
        ],
      },
      {
        title: 'How We Use Your Data',
        body: 'Data is processed to operate the clinic, deliver secure tele-dentistry, and send helpful reminders.',
        items: [
          'Confirm and manage bookings or treatment plans',
          'Provide patient support via email, phone, or chat',
          'Fulfill legal, regulatory, and billing requirements',
          'Improve our services through aggregated analytics',
        ],
      },
      {
        title: 'Your Choices & Rights',
        body: 'You can review, update, or request deletion of your data at any time. We respond to verified requests within 30 days.',
        items: [
          'Update account details from the patient dashboard',
          'Contact our care team to export records or revoke consent',
          'Adjust marketing preferences inside notification settings',
        ],
      },
    ],
  },
  ar: {
    title: 'سياسة الخصوصية',
    intro: 'نحترم ثقتك ونوضح هنا كيفية جمع معلومات المرضى وحمايتها واستخدامها عبر جميع خدماتنا الرقمية.',
    updated: 'آخر تحديث: 1 مارس 2025',
    sections: [
      {
        title: 'المعلومات التي نجمعها',
        body: 'نقوم بجمع البيانات الضرورية فقط لتقديم الرعاية السنية، وإدارة المواعيد، وتخصيص تجربتك.',
        items: [
          'بيانات التواصل مثل الاسم والبريد الإلكتروني ورقم الهاتف',
          'تفضيلات المواعيد والخدمات المختارة وملاحظات الطبيب',
          'معلومات التأمين أو الدفع المقدمة أثناء الحجز',
          'تحليلات الاستخدام التي تساعدنا على تحسين الأداء',
        ],
      },
      {
        title: 'كيفية استخدام بياناتك',
        body: 'يتم معالجة البيانات لتشغيل العيادة، وتقديم خدمات الطب عن بعد بأمان، وإرسال التذكيرات المفيدة.',
        items: [
          'تأكيد المواعيد وإدارتها وخطط العلاج',
          'تقديم الدعم للمرضى عبر البريد أو الهاتف أو الدردشة',
          'الامتثال للمتطلبات القانونية والتنظيمية والفوترة',
          'تحسين خدماتنا من خلال تحليلات مجمعة',
        ],
      },
      {
        title: 'خياراتك وحقوقك',
        body: 'يمكنك مراجعة بياناتك أو تحديثها أو طلب حذفها في أي وقت. نستجيب للطلبات الموثقة خلال 30 يومًا.',
        items: [
          'تحديث بيانات الحساب من لوحة المريض',
          'التواصل مع فريق الرعاية لطلب نسخة من السجلات أو سحب الموافقة',
          'تعديل تفضيلات الرسائل من إعدادات التنبيهات',
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
  const content = PRIVACY_CONTENT[locale];

  return buildLocalizedMetadata({
    locale: params.locale,
    path: '/privacy',
    title: content.title,
    description: content.intro,
    keywords: ['privacy policy', 'patient data', 'medical privacy'],
  });
}

export default function PrivacyPage({ params }: PageProps) {
  const locale = resolveLocale(params.locale);
  const content = PRIVACY_CONTENT[locale];

  return <LegalPage {...content} />;
}
