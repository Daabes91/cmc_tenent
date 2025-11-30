/**
 * Healthcare-specific content configuration
 * Centralized content management for easy updates without code changes
 * Supports English and Arabic languages
 */

import { HealthcareContent } from './types';

export const healthcareCopy: HealthcareContent = {
  en: {
    hero: {
      badge: "Launch your clinic online — in 1 day, not months",
      headline: {
        prefix: "Your All-in-One Platform to Run Your Clinic",
        highlight: "Your website, admin dashboard, and patient tools — all in one place"
      },
      description: "Stop relying on scattered tools and manual work. Get a professional website, an easy-to-use admin dashboard, and everything you need to manage appointments, patients, doctors, services, and online consultations — all from one simple and secure platform.",
      ctaPrimary: "Get Your Clinic Portal",
      ctaSecondary: "Book a Demo",
      trustLabel: "Trusted by healthcare providers worldwide",
      companyLogos: [
        "dailydev",
        "ycombinator",
        "bestofjs",
        "product-hunt",
        "reddit",
        "launchtory",
        "medium",
        "devto"
      ]
    },
    features: {
      title: "Everything your clinic needs to thrive",
      subtitle: "We bundle the polished website your patients expect with the admin tools your team needs, so you can focus on care—not software.",
      items: [
        {
          icon: "Globe",
          title: "Your own clinic website",
          description: "Pick a modern theme, add your logo, and go live on your own domain in minutes.",
          benefits: [
            "Professional healthcare-focused design",
            "Custom domain support",
            "Mobile-responsive layouts",
            "SEO-optimized for local search"
          ]
        },
        {
          icon: "CalendarCheck",
          title: "Smart Appointment Scheduling",
          description: "Eliminate double-bookings and no-shows with intelligent scheduling",
          benefits: [
            "Automated appointment reminders",
            "Online booking for patients 24/7",
            "Provider availability management",
            "Waitlist automation"
          ]
        },
        {
          icon: "Users",
          title: "Complete Patient Management",
          description: "Centralize patient records, history, and communications",
          benefits: [
            "Digital patient records",
            "Treatment history tracking",
            "Secure document storage",
            "Patient portal access"
          ]
        },
        {
          icon: "DollarSign",
          title: "Automated Billing & Invoicing",
          description: "Streamline payments and reduce billing errors",
          benefits: [
            "Automated invoice generation",
            "Payment tracking",
            "Insurance claim management",
            "Financial reporting"
          ]
        },
        {
          icon: "MonitorSmartphone",
          title: "Virtual Care Ready",
          description: "Offer secure video consultations, collect payments, and deliver receipts automatically.",
          benefits: [
            "HIPAA-compliant video consultations",
            "Integrated payment processing",
            "Automated receipt delivery",
            "Secure messaging"
          ]
        },
        {
          icon: "BarChart",
          title: "Practice Analytics & Reporting",
          description: "Make data-driven decisions with comprehensive insights",
          benefits: [
            "Revenue tracking",
            "Patient demographics",
            "Appointment analytics",
            "Staff performance metrics"
          ]
        }
      ]
    },
    testimonials: {
      title: "Trusted by healthcare professionals",
      subtitle: "From solo practitioners to multi-location clinics, healthcare providers trust our platform to manage their practice efficiently.",
      items: [
        {
          name: "Dr. Sarah Johnson",
          role: "Clinic Director",
          clinicType: "Family Medicine Practice",
          quote: "This platform transformed how we manage our clinic. We've reduced scheduling conflicts by 80% and our patients love the online booking feature.",
          metric: "80% reduction in scheduling conflicts",
          avatar: "/images/testimonials/dr-sarah-johnson.jpg"
        },
        {
          name: "Michael Chen",
          role: "Practice Manager",
          clinicType: "Dental Clinic",
          quote: "The billing automation alone has saved us 15 hours per week. Our staff can now focus on patient care instead of paperwork.",
          metric: "15 hours saved per week",
          avatar: "/images/testimonials/michael-chen.jpg"
        },
        {
          name: "Dr. Aisha Al-Rashid",
          role: "Owner",
          clinicType: "Physical Therapy Center",
          quote: "Patient satisfaction scores increased by 35% after implementing the patient portal. Communication has never been easier.",
          metric: "35% increase in patient satisfaction",
          avatar: "/images/testimonials/dr-aisha-alrashid.jpg"
        },
        {
          name: "Dr. Layla Qadri",
          role: "Medical Director",
          clinicType: "Qadri Dental Group",
          quote: "Rolling out this platform gave us a bilingual marketing site, automated reminders, and PayPal checkout in the same week. Our no-show rate dropped 28% immediately.",
          metric: "28% reduction in no-shows",
          avatar: "/images/testimonials/dr-layla-qadri.jpg"
        },
        {
          name: "Sameer Haddad",
          role: "COO",
          clinicType: "Shifa Health Network",
          quote: "Tenant isolation and the SAAS admin let us manage 11 locations without spinning up a new stack every time. Finance finally has a single source of truth for revenue.",
          metric: "11 locations managed seamlessly",
          avatar: "/images/testimonials/sameer-haddad.jpg"
        }
      ]
    },
    pricing: {
      title: "Flexible pricing for every clinic phase",
      subtitle: "Licensing is per clinic location so you only pay for the people you actually serve.",
      tiers: [
        {
          name: "Solo Practice",
          description: "Perfect for individual practitioners starting their digital journey",
          price: {
            monthly: 49,
            annual: 470
          },
          features: [
            "1 provider account",
            "Up to 100 patients",
            "500 appointments/month",
            "Basic reporting",
            "Email support",
            "Custom domain",
            "Patient portal"
          ],
          limits: {
            providers: 1,
            patients: 100,
            appointments: 500
          },
          cta: "Start Free Trial"
        },
        {
          name: "Small Clinic",
          description: "For growing practices with multiple providers",
          price: {
            monthly: 149,
            annual: 1430
          },
          features: [
            "Up to 5 providers",
            "Up to 500 patients",
            "2000 appointments/month",
            "Advanced reporting",
            "Priority support",
            "Custom branding",
            "Video consultations",
            "Insurance billing"
          ],
          limits: {
            providers: 5,
            patients: 500,
            appointments: 2000
          },
          cta: "Start Free Trial",
          popular: true
        },
        {
          name: "Multi-Location Practice",
          description: "For established healthcare organizations with multiple locations",
          price: {
            monthly: 399,
            annual: 3830
          },
          features: [
            "Unlimited providers",
            "Unlimited patients",
            "Unlimited appointments",
            "Custom integrations",
            "Dedicated account manager",
            "White-label options",
            "API access",
            "Advanced analytics",
            "HIPAA compliance documentation"
          ],
          limits: {
            providers: -1,
            patients: -1,
            appointments: -1
          },
          cta: "Contact Sales"
        }
      ]
    },
    integrations: {
      title: "Integrates with your healthcare workflow",
      subtitle: "Connect with the tools you already use to streamline your clinic operations",
      items: [
        {
          name: "Stripe",
          description: "Accept patient payments securely with automated billing for clinic services",
          logo: "/images/integrations/stripe.svg",
          category: "payment"
        },
        {
          name: "PayPal",
          description: "Process clinic payments and manage subscription billing for treatment plans",
          logo: "/images/integrations/paypal.svg",
          category: "payment"
        },
        {
          name: "Google Calendar",
          description: "Sync appointment schedules with provider calendars for seamless clinic coordination",
          logo: "/images/integrations/google-calendar.svg",
          category: "calendar"
        },
        {
          name: "Mailchimp",
          description: "Send appointment reminders and health tips to patients automatically",
          logo: "/images/integrations/mailchimp.svg",
          category: "email"
        },
        {
          name: "QuickBooks",
          description: "Sync clinic revenue and expenses for accurate healthcare practice accounting",
          logo: "/images/integrations/quickbooks.svg",
          category: "accounting"
        },
        {
          name: "Twilio",
          description: "Send SMS appointment reminders and notifications to patients",
          logo: "/images/integrations/twilio.svg",
          category: "other"
        }
      ],
      ctaText: "View All Integrations"
    },
    security: {
      title: "Healthcare-grade security and compliance",
      description: "Your patient data is protected with enterprise-level security measures and healthcare compliance standards",
      badges: [
        "HIPAA Compliant",
        "GDPR Ready",
        "SOC 2 Type II",
        "ISO 27001"
      ],
      trustIndicators: [
        "Bank-level 256-bit encryption",
        "Regular security audits",
        "Automated daily backups",
        "99.9% uptime SLA",
        "Role-based access control",
        "Audit trail logging"
      ]
    }
  },
  ar: {
    hero: {
      badge: "أطلق عيادتك على الإنترنت — في يوم واحد، وليس شهوراً",
      headline: {
        prefix: "منصتك الشاملة لإدارة عيادتك",
        highlight: "موقعك الإلكتروني، لوحة التحكم، وأدوات المرضى — كل شيء في مكان واحد"
      },
      description: "توقف عن الاعتماد على أدوات متفرقة وعمل يدوي. احصل على موقع إلكتروني احترافي، ولوحة تحكم سهلة الاستخدام، وكل ما تحتاجه لإدارة المواعيد والمرضى والأطباء والخدمات والاستشارات عبر الإنترنت — كل ذلك من منصة واحدة بسيطة وآمنة.",
      ctaPrimary: "احصل على بوابة عيادتك",
      ctaSecondary: "احجز عرضاً توضيحياً",
      trustLabel: "موثوق به من قبل مقدمي الرعاية الصحية في جميع أنحاء العالم",
      companyLogos: [
        "dailydev",
        "ycombinator",
        "bestofjs",
        "product-hunt",
        "reddit",
        "launchtory",
        "medium",
        "devto"
      ]
    },
    features: {
      title: "كل ما تحتاجه عيادتك للنمو",
      subtitle: "نقدم لك الموقع الأنيق الذي يطمئن المرضى ولوحة التحكم التي تسهل عمل الفريق، لتتفرغ للرعاية بدلاً من الانشغال بالأدوات.",
      items: [
        {
          icon: "Globe",
          title: "موقع خاص بعيادتك",
          description: "اختر قالباً عصرياً، أضف شعارك، وانطلق على نطاقك خلال دقائق.",
          benefits: [
            "تصميم احترافي مخصص للرعاية الصحية",
            "دعم النطاق المخصص",
            "تخطيطات متجاوبة مع الأجهزة المحمولة",
            "محسّن لمحركات البحث المحلية"
          ]
        },
        {
          icon: "CalendarCheck",
          title: "جدولة مواعيد ذكية",
          description: "تخلص من تضارب المواعيد والمواعيد الملغاة بجدولة ذكية",
          benefits: [
            "تذكيرات تلقائية بالمواعيد",
            "حجز عبر الإنترنت للمرضى على مدار الساعة",
            "إدارة توفر مقدمي الخدمة",
            "أتمتة قائمة الانتظار"
          ]
        },
        {
          icon: "Users",
          title: "إدارة شاملة للمرضى",
          description: "مركزية سجلات المرضى والتاريخ والاتصالات",
          benefits: [
            "سجلات المرضى الرقمية",
            "تتبع تاريخ العلاج",
            "تخزين آمن للمستندات",
            "وصول بوابة المريض"
          ]
        },
        {
          icon: "DollarSign",
          title: "الفوترة والفواتير الآلية",
          description: "تبسيط المدفوعات وتقليل أخطاء الفوترة",
          benefits: [
            "إنشاء الفواتير تلقائياً",
            "تتبع المدفوعات",
            "إدارة مطالبات التأمين",
            "التقارير المالية"
          ]
        },
        {
          icon: "MonitorSmartphone",
          title: "جاهز للرعاية الافتراضية",
          description: "قدم استشارات فيديو آمنة، واجمع المدفوعات، وقدم الإيصالات تلقائياً.",
          benefits: [
            "استشارات فيديو متوافقة مع HIPAA",
            "معالجة الدفع المتكاملة",
            "تسليم الإيصالات تلقائياً",
            "المراسلة الآمنة"
          ]
        },
        {
          icon: "BarChart",
          title: "تحليلات وتقارير الممارسة",
          description: "اتخذ قرارات مستنيرة بالبيانات مع رؤى شاملة",
          benefits: [
            "تتبع الإيرادات",
            "التركيبة السكانية للمرضى",
            "تحليلات المواعيد",
            "مقاييس أداء الموظفين"
          ]
        }
      ]
    },
    testimonials: {
      title: "موثوق به من قبل المتخصصين في الرعاية الصحية",
      subtitle: "من الممارسين الفرديين إلى العيادات متعددة المواقع، يثق مقدمو الرعاية الصحية في منصتنا لإدارة ممارساتهم بكفاءة.",
      items: [
        {
          name: "د. سارة جونسون",
          role: "مديرة العيادة",
          clinicType: "عيادة طب الأسرة",
          quote: "حولت هذه المنصة طريقة إدارتنا للعيادة. لقد قللنا تضارب المواعيد بنسبة 80٪ ويحب مرضانا ميزة الحجز عبر الإنترنت.",
          metric: "انخفاض بنسبة 80٪ في تضارب المواعيد",
          avatar: "/images/testimonials/dr-sarah-johnson.jpg"
        },
        {
          name: "مايكل تشين",
          role: "مدير الممارسة",
          clinicType: "عيادة الأسنان",
          quote: "أتمتة الفوترة وحدها وفرت لنا 15 ساعة في الأسبوع. يمكن لموظفينا الآن التركيز على رعاية المرضى بدلاً من الأعمال الورقية.",
          metric: "توفير 15 ساعة في الأسبوع",
          avatar: "/images/testimonials/michael-chen.jpg"
        },
        {
          name: "د. عائشة الرشيد",
          role: "المالكة",
          clinicType: "مركز العلاج الطبيعي",
          quote: "زادت درجات رضا المرضى بنسبة 35٪ بعد تطبيق بوابة المريض. لم يكن التواصل أسهل من أي وقت مضى.",
          metric: "زيادة بنسبة 35٪ في رضا المرضى",
          avatar: "/images/testimonials/dr-aisha-alrashid.jpg"
        },
        {
          name: "د. ليلى قادري",
          role: "المديرة الطبية",
          clinicType: "مجموعة قادري لطب الأسنان",
          quote: "أعطانا إطلاق هذه المنصة موقعاً تسويقياً ثنائي اللغة، وتذكيرات تلقائية، ودفع PayPal في نفس الأسبوع. انخفض معدل عدم الحضور لدينا بنسبة 28٪ على الفور.",
          metric: "انخفاض بنسبة 28٪ في عدم الحضور",
          avatar: "/images/testimonials/dr-layla-qadri.jpg"
        },
        {
          name: "سمير حداد",
          role: "المدير التنفيذي للعمليات",
          clinicType: "شبكة الشفاء الصحية",
          quote: "عزل المستأجرين ومسؤول SAAS يتيح لنا إدارة 11 موقعاً دون إنشاء مجموعة جديدة في كل مرة. أخيراً لدى المالية مصدر واحد للحقيقة للإيرادات.",
          metric: "إدارة 11 موقعاً بسلاسة",
          avatar: "/images/testimonials/sameer-haddad.jpg"
        }
      ]
    },
    pricing: {
      title: "أسعار مرنة لكل مرحلة من مراحل العيادة",
      subtitle: "الترخيص لكل موقع عيادة، لذا تدفع فقط مقابل الأشخاص الذين تخدمهم بالفعل.",
      tiers: [
        {
          name: "ممارسة فردية",
          description: "مثالي للممارسين الأفراد الذين يبدأون رحلتهم الرقمية",
          price: {
            monthly: 49,
            annual: 470
          },
          features: [
            "حساب مزود واحد",
            "حتى 100 مريض",
            "500 موعد / شهر",
            "تقارير أساسية",
            "دعم البريد الإلكتروني",
            "نطاق مخصص",
            "بوابة المريض"
          ],
          limits: {
            providers: 1,
            patients: 100,
            appointments: 500
          },
          cta: "ابدأ الفترة التجريبية المجانية"
        },
        {
          name: "عيادة صغيرة",
          description: "للممارسات المتنامية مع مقدمي خدمات متعددين",
          price: {
            monthly: 149,
            annual: 1430
          },
          features: [
            "حتى 5 مقدمي خدمة",
            "حتى 500 مريض",
            "2000 موعد / شهر",
            "تقارير متقدمة",
            "دعم ذو أولوية",
            "علامة تجارية مخصصة",
            "استشارات الفيديو",
            "فوترة التأمين"
          ],
          limits: {
            providers: 5,
            patients: 500,
            appointments: 2000
          },
          cta: "ابدأ الفترة التجريبية المجانية",
          popular: true
        },
        {
          name: "ممارسة متعددة المواقع",
          description: "للمؤسسات الصحية الراسخة مع مواقع متعددة",
          price: {
            monthly: 399,
            annual: 3830
          },
          features: [
            "مقدمو خدمة غير محدودين",
            "مرضى غير محدودين",
            "مواعيد غير محدودة",
            "تكاملات مخصصة",
            "مدير حساب مخصص",
            "خيارات العلامة البيضاء",
            "وصول API",
            "تحليلات متقدمة",
            "وثائق الامتثال لـ HIPAA"
          ],
          limits: {
            providers: -1,
            patients: -1,
            appointments: -1
          },
          cta: "اتصل بالمبيعات"
        }
      ]
    },
    integrations: {
      title: "يتكامل مع سير عمل الرعاية الصحية الخاص بك",
      subtitle: "اتصل بالأدوات التي تستخدمها بالفعل لتبسيط عمليات عيادتك",
      items: [
        {
          name: "Stripe",
          description: "قبول مدفوعات المرضى بشكل آمن مع الفوترة الآلية لخدمات العيادة",
          logo: "/images/integrations/stripe.svg",
          category: "payment"
        },
        {
          name: "PayPal",
          description: "معالجة مدفوعات العيادة وإدارة فوترة الاشتراك لخطط العلاج",
          logo: "/images/integrations/paypal.svg",
          category: "payment"
        },
        {
          name: "Google Calendar",
          description: "مزامنة جداول المواعيد مع تقويمات مقدمي الخدمة لتنسيق سلس للعيادة",
          logo: "/images/integrations/google-calendar.svg",
          category: "calendar"
        },
        {
          name: "Mailchimp",
          description: "إرسال تذكيرات المواعيد ونصائح صحية للمرضى تلقائياً",
          logo: "/images/integrations/mailchimp.svg",
          category: "email"
        },
        {
          name: "QuickBooks",
          description: "مزامنة إيرادات ونفقات العيادة للمحاسبة الدقيقة لممارسة الرعاية الصحية",
          logo: "/images/integrations/quickbooks.svg",
          category: "accounting"
        },
        {
          name: "Twilio",
          description: "إرسال تذكيرات المواعيد والإشعارات عبر الرسائل القصيرة للمرضى",
          logo: "/images/integrations/twilio.svg",
          category: "other"
        }
      ],
      ctaText: "عرض جميع التكاملات"
    },
    security: {
      title: "أمان وامتثال على مستوى الرعاية الصحية",
      description: "بيانات مرضاك محمية بتدابير أمنية على مستوى المؤسسات ومعايير امتثال الرعاية الصحية",
      badges: [
        "متوافق مع HIPAA",
        "جاهز لـ GDPR",
        "SOC 2 Type II",
        "ISO 27001"
      ],
      trustIndicators: [
        "تشفير 256 بت على مستوى البنوك",
        "عمليات تدقيق أمنية منتظمة",
        "نسخ احتياطية يومية تلقائية",
        "اتفاقية مستوى الخدمة بنسبة 99.9٪",
        "التحكم في الوصول على أساس الدور",
        "تسجيل مسار التدقيق"
      ]
    }
  }
};
