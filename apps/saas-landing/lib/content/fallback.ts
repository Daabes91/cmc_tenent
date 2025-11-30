/**
 * Fallback Content for Error Handling
 * 
 * Provides minimal viable content when primary content fails to load
 */

import { HealthcareContent } from './types';

/**
 * Minimal fallback content for critical sections
 */
export const fallbackContent: HealthcareContent = {
  en: {
    hero: {
      badge: "Healthcare Practice Management",
      headline: {
        prefix: "Streamline Your Clinic Operations",
        highlight: "All-in-one platform for healthcare providers"
      },
      description: "Manage appointments, patients, billing, and more with our comprehensive clinic management solution.",
      ctaPrimary: "Get Started",
      ctaSecondary: "Learn More",
      trustLabel: "Trusted by healthcare providers",
      companyLogos: []
    },
    features: {
      title: "Essential Features",
      subtitle: "Everything you need to run your clinic efficiently",
      items: [
        {
          icon: "CalendarCheck",
          title: "Appointment Scheduling",
          description: "Manage your clinic schedule efficiently",
          benefits: [
            "Online booking",
            "Automated reminders",
            "Calendar sync"
          ]
        },
        {
          icon: "Users",
          title: "Patient Management",
          description: "Keep track of patient records and history",
          benefits: [
            "Digital records",
            "Secure storage",
            "Easy access"
          ]
        },
        {
          icon: "DollarSign",
          title: "Billing & Payments",
          description: "Streamline your clinic's financial operations",
          benefits: [
            "Invoice generation",
            "Payment tracking",
            "Financial reports"
          ]
        }
      ]
    },
    testimonials: {
      title: "What Our Users Say",
      subtitle: "Healthcare professionals trust our platform",
      items: [
        {
          name: "Healthcare Professional",
          role: "Clinic Manager",
          clinicType: "Medical Practice",
          quote: "This platform has improved our clinic operations significantly.",
          metric: "Improved efficiency",
          avatar: "/images/testimonials/placeholder.jpg"
        }
      ]
    },
    pricing: {
      title: "Simple Pricing",
      subtitle: "Choose the plan that fits your clinic",
      tiers: [
        {
          name: "Starter",
          description: "For small practices",
          price: {
            monthly: 49,
            annual: 470
          },
          features: [
            "Basic features",
            "Email support",
            "Up to 100 patients"
          ],
          limits: {
            providers: 1,
            patients: 100,
            appointments: 500
          },
          cta: "Get Started"
        },
        {
          name: "Professional",
          description: "For growing clinics",
          price: {
            monthly: 149,
            annual: 1430
          },
          features: [
            "All features",
            "Priority support",
            "Up to 500 patients"
          ],
          limits: {
            providers: 5,
            patients: 500,
            appointments: 2000
          },
          cta: "Get Started",
          popular: true
        }
      ]
    },
    integrations: {
      title: "Integrations",
      subtitle: "Connect with your favorite tools",
      items: [],
      ctaText: "View Integrations"
    },
    security: {
      title: "Secure & Compliant",
      description: "Your data is protected with enterprise-level security",
      badges: ["HIPAA Compliant", "GDPR Ready"],
      trustIndicators: [
        "Bank-level encryption",
        "Regular backups",
        "99.9% uptime"
      ]
    }
  },
  ar: {
    hero: {
      badge: "إدارة الممارسات الصحية",
      headline: {
        prefix: "تبسيط عمليات عيادتك",
        highlight: "منصة شاملة لمقدمي الرعاية الصحية"
      },
      description: "إدارة المواعيد والمرضى والفواتير والمزيد مع حل إدارة العيادة الشامل لدينا.",
      ctaPrimary: "ابدأ الآن",
      ctaSecondary: "اعرف المزيد",
      trustLabel: "موثوق به من قبل مقدمي الرعاية الصحية",
      companyLogos: []
    },
    features: {
      title: "الميزات الأساسية",
      subtitle: "كل ما تحتاجه لإدارة عيادتك بكفاءة",
      items: [
        {
          icon: "CalendarCheck",
          title: "جدولة المواعيد",
          description: "إدارة جدول عيادتك بكفاءة",
          benefits: [
            "الحجز عبر الإنترنت",
            "تذكيرات تلقائية",
            "مزامنة التقويم"
          ]
        },
        {
          icon: "Users",
          title: "إدارة المرضى",
          description: "تتبع سجلات المرضى وتاريخهم",
          benefits: [
            "سجلات رقمية",
            "تخزين آمن",
            "وصول سهل"
          ]
        },
        {
          icon: "DollarSign",
          title: "الفواتير والمدفوعات",
          description: "تبسيط العمليات المالية لعيادتك",
          benefits: [
            "إنشاء الفواتير",
            "تتبع المدفوعات",
            "التقارير المالية"
          ]
        }
      ]
    },
    testimonials: {
      title: "ماذا يقول مستخدمونا",
      subtitle: "يثق المتخصصون في الرعاية الصحية في منصتنا",
      items: [
        {
          name: "متخصص في الرعاية الصحية",
          role: "مدير العيادة",
          clinicType: "ممارسة طبية",
          quote: "حسنت هذه المنصة عمليات عيادتنا بشكل كبير.",
          metric: "تحسين الكفاءة",
          avatar: "/images/testimonials/placeholder.jpg"
        }
      ]
    },
    pricing: {
      title: "أسعار بسيطة",
      subtitle: "اختر الخطة التي تناسب عيادتك",
      tiers: [
        {
          name: "المبتدئ",
          description: "للممارسات الصغيرة",
          price: {
            monthly: 49,
            annual: 470
          },
          features: [
            "الميزات الأساسية",
            "دعم البريد الإلكتروني",
            "حتى 100 مريض"
          ],
          limits: {
            providers: 1,
            patients: 100,
            appointments: 500
          },
          cta: "ابدأ الآن"
        },
        {
          name: "المحترف",
          description: "للعيادات المتنامية",
          price: {
            monthly: 149,
            annual: 1430
          },
          features: [
            "جميع الميزات",
            "دعم ذو أولوية",
            "حتى 500 مريض"
          ],
          limits: {
            providers: 5,
            patients: 500,
            appointments: 2000
          },
          cta: "ابدأ الآن",
          popular: true
        }
      ]
    },
    integrations: {
      title: "التكاملات",
      subtitle: "اتصل بأدواتك المفضلة",
      items: [],
      ctaText: "عرض التكاملات"
    },
    security: {
      title: "آمن ومتوافق",
      description: "بياناتك محمية بأمان على مستوى المؤسسات",
      badges: ["متوافق مع HIPAA", "جاهز لـ GDPR"],
      trustIndicators: [
        "تشفير على مستوى البنوك",
        "نسخ احتياطية منتظمة",
        "وقت تشغيل 99.9٪"
      ]
    }
  }
};

/**
 * Get fallback content for a specific language
 */
export function getFallbackCopy(language: 'en' | 'ar' = 'en'): HealthcareContent[typeof language] {
  return fallbackContent[language];
}
