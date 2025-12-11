"use client"

import { useState } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { ChevronDown } from "lucide-react"
import { cn } from "@/lib/utils"
import {useLanguage} from '@/contexts/LanguageContext';

const SECTION_COPY = {
  en: {
    badge: 'Questions & Answers',
    title: 'Frequently Asked Questions',
    subtitle: 'Everything clinics ask us about launching, customization, and support.',
    contact: 'Still have questions?',
    contactLink: 'Contact our support team',
  },
  ar: {
    badge: 'الأسئلة الشائعة',
    title: 'إجابات على أكثر الأسئلة تكراراً',
    subtitle: 'كل ما تسألنا عنه العيادات حول الإطلاق والتخصيص والدعم.',
    contact: 'هل لديك أسئلة أخرى؟',
    contactLink: 'تواصل مع فريق الدعم',
  },
} as const;

const FAQS = {
  en: [
    {
      question: "How long does it take to launch?",
      answer:
        "Most clinics are live within a day. Pick a theme, add your content, connect a domain, and your website + admin panel are ready for patients.",
    },
    {
      question: "Can we use our own domain and branding?",
      answer:
        "Yes. Upload your logo, colors, and photography, then point your domain at the platform. Every clinic gets a fully branded experience.",
    },
    {
      question: "Do we need developers or IT?",
      answer:
        "Nope. Everything is managed through a simple dashboard, including content updates, schedules, services, and automation rules.",
    },
    {
      question: "What about virtual care and payments?",
      answer:
        "Virtual visits, secure payments, and automated reminders are built in. You can activate PayPal, card-on-file, or offline payments whenever you’re ready.",
    },
    {
      question: "Can you migrate our current data?",
      answer:
        "Absolutely. We import patients, services, and bookings from spreadsheets or your existing system so you start with a complete, organized workspace.",
    },
    {
      question: "Is support included?",
      answer:
        "All plans include onboarding and customer support. Growth and Enterprise plans add priority response times, training, and dedicated specialists.",
    },
  ],
  ar: [
    {
      question: "كم يستغرق الإطلاق؟",
      answer:
        "معظم العيادات تنطلق خلال يوم واحد. اختر القالب، أضف المحتوى، اربط النطاق، وستكون الموقع ولوحة التحكم جاهزين للمرضى.",
    },
    {
      question: "هل يمكننا استخدام نطاق وهوية العيادة؟",
      answer:
        "بالتأكيد. ارفع الشعار والألوان والصور ثم اربط نطاقك بالموقع لتحصل على تجربة تحمل هوية عيادتك بالكامل.",
    },
    {
      question: "هل نحتاج إلى فريق تقني؟",
      answer:
        "لا حاجة لذلك. كل شيء يتم من خلال لوحة تحكم بسيطة تشمل تحديث المحتوى والجداول والخدمات والتذكيرات الآلية.",
    },
    {
      question: "وماذا عن الزيارات الافتراضية والمدفوعات؟",
      answer:
        "المنصة تدعم الاستشارات بالفيديو والمدفوعات الآمنة والتذكيرات الآلية. يمكنك تفعيل باي بال أو البطاقة أو الدفع في العيادة وقتما تشاء.",
    },
    {
      question: "هل يمكنكم ترحيل بياناتنا الحالية؟",
      answer:
        "نعم. نستورد بيانات المرضى والخدمات والحجوزات من الجداول أو النظام الحالي لتبدأ بلوحة منظمة كاملة.",
    },
    {
      question: "هل يشمل الاشتراك الدعم؟",
      answer:
        "كل الباقات تتضمن الإعداد والدعم. باقات نمو ومؤسسات تضيف أوقات استجابة أولوية وتدريباً وخبراء مخصصين.",
    },
  ],
} as const;

export default function Faq() {
  const {language} = useLanguage();
  const faqs = FAQS[language];
  const copy = SECTION_COPY[language];

  const [activeIndex, setActiveIndex] = useState<number | null>(null)

  const toggleFaq = (index: number) => {
    setActiveIndex(activeIndex === index ? null : index)
  }

  return (
    <section id="faq" className="relative py-20 md:py-32 overflow-hidden">
      {/* Background elements */}
      <div className="absolute inset-0 bg-gradient-to-b from-slate-50 via-white to-slate-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950"></div>

      {/* Decorative elements */}
      <div className="absolute top-0 right-0 w-1/3 h-1/3 bg-mintlify-blue/15 rounded-full blur-3xl"></div>
      <div className="absolute bottom-0 left-0 w-1/3 h-1/3 bg-primary/15 rounded-full blur-3xl"></div>

      <div className="container relative px-4 md:px-8">
        <div className="max-w-3xl mx-auto text-center mb-16">
          <p className="text-primary dark:text-primary/80 font-medium mb-2">{copy.badge}</p>
          <h2 className="text-3xl md:text-4xl font-bold mb-6">{copy.title}</h2>
          <p className="text-slate-600 dark:text-gray-400 text-lg">
            {copy.subtitle}
          </p>
        </div>

        <div className="max-w-3xl mx-auto">
          <div className="space-y-4">
            {faqs.map((faq, index) => (
              <div
                key={index}
                className="border border-slate-200 dark:border-gray-800 rounded-lg overflow-hidden bg-white dark:bg-gray-900/50 backdrop-blur-sm"
              >
                <button
                  className="flex justify-between items-center w-full p-6 text-left"
                  onClick={() => toggleFaq(index)}
                >
                  <span className="font-medium text-lg text-slate-900 dark:text-white">{faq.question}</span>
                  <ChevronDown
                    className={cn(
                      "h-5 w-5 text-primary dark:text-primary/80 transition-transform duration-300",
                      activeIndex === index ? "transform rotate-180" : "",
                    )}
                  />
                </button>
                <AnimatePresence>
                  {activeIndex === index && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.3 }}
                    >
                      <div className="px-6 pb-6 text-slate-600 dark:text-gray-400">{faq.answer}</div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            ))}
          </div>
        </div>

        <div className="mt-12 text-center">
          <p className="text-slate-600 dark:text-gray-400">
            {copy.contact}{" "}
            <a href="mailto:info@cliniqax.com" className="text-primary dark:text-primary/80 hover:text-primary/70 dark:hover:text-primary font-medium">
              {copy.contactLink}
            </a>
          </p>
        </div>
      </div>
    </section>
  )
}
