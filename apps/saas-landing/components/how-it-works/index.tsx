"use client"

import { useRef, useEffect } from "react"
import { motion, useInView, useAnimation } from "framer-motion"
import { ArrowRight, CheckCircle, ChevronRight } from "lucide-react"
import Image from "next/image"
import {useLanguage} from '@/contexts/LanguageContext';

const content = {
  en: {
    badge: 'Launch in four easy steps',
    title: 'How CMC Platform goes live in under a week',
    description: 'Follow the same rollout clinics across dental, primary care, and tele-health use—no custom dev sprint required.',
    cta: 'Partner with our implementation team',
    ctaSub: 'We migrate data, import historical appointments, and train staff under one statement of work.',
    steps: [
      {
        number: "01",
        title: "Pick your clinic style",
        description: "Choose a website layout, drop in your logo and colors, and claim your domain.",
        image: "/images/dashboard.png",
        bullets: [
          "Beautiful templates built for healthcare",
          "Works in any language and on every device",
          "Connect your domain or use ours to launch instantly",
        ],
      },
      {
        number: "02",
        title: "Add services & team details",
        description: "Use the admin panel to add doctors, services, FAQs, and marketing copy.",
        image: "/images/team.png",
        bullets: [
          "Drag-and-drop sections for hero, services, and pricing",
          "Service, doctor, and testimonial libraries",
          "Preview changes before publishing live",
        ],
      },
      {
        number: "03",
        title: "Switch on bookings & payments",
        description: "Accept appointments online, route them to doctors, and capture payments securely.",
        image: "/images/webinar.png",
        bullets: [
          "Online scheduling with automated reminders",
          "Virtual consultations with built-in video links",
          "PayPal, card, and in-clinic payment options",
        ],
      },
      {
        number: "04",
        title: "Run everything from one place",
        description: "Use the dashboard to manage patients, track performance, and grow with confidence.",
        image: "/images/automation.png",
        bullets: [
          "Patient, doctor, and service management in one view",
          "Real-time insights on bookings and revenue",
          "Tools that scale from solo practices to multi-clinic brands",
        ],
      },
    ],
  },
  ar: {
    badge: 'أربع خطوات سهلة للإطلاق',
    title: 'كيف ننشر منصتك خلال أقل من أسبوع',
    description: 'نتّبع نفس خطة الإطلاق التي تعتمدها العيادات في طب الأسنان والرعاية الأولية والطب عن بعد—بدون جهد برمجي.',
    cta: 'انضم لفريق التنفيذ لدينا',
    ctaSub: 'نقوم بترحيل بياناتك، واستيراد المواعيد السابقة، وتدريب الفريق ضمن عقد واحد واضح.',
    steps: [
      {
        number: "01",
        title: "اختر هوية موقعك",
        description: "انتقِ التصميم المناسب، أضف شعارك وألوانك، وحدد النطاق الذي ستنطلق منه.",
        image: "/images/dashboard.png",
        bullets: [
          "قوالب مصممة خصيصاً للقطاع الصحي",
          "تعمل بجميع اللغات وعلى كل الأجهزة",
          "اربط نطاقك أو استخدم نطاقنا للانطلاق فوراً",
        ],
      },
      {
        number: "02",
        title: "أضف الخدمات وفريق العمل",
        description: "من لوحة التحكم أضف الأطباء والخدمات والأسئلة الشائعة والمحتوى التسويقي.",
        image: "/images/team.png",
        bullets: [
          "أقسام قابلة للتحريك وتعديل النصوص بسهولة",
          "مكتبة للخدمات والأطباء والشهادات",
          "عاين التغييرات قبل نشرها للجمهور",
        ],
      },
      {
        number: "03",
        title: "فعّل الحجوزات والمدفوعات",
        description: "استقبل المواعيد عبر الإنترنت، وجّهها للأطباء المناسبين، واقبل المدفوعات بأمان.",
        image: "/images/webinar.png",
        bullets: [
          "جدولة إلكترونية مع تذكيرات تلقائية",
          "استشارات افتراضية بروابط فيديو مدمجة",
          "خيارات دفع عبر باي بال أو البطاقة أو الكاش",
        ],
      },
      {
        number: "04",
        title: "أدر كل شيء من لوحة واحدة",
        description: "استخدم اللوحة لمتابعة المرضى والأداء وتوسيع خدماتك بثقة.",
        image: "/images/automation.png",
        bullets: [
          "إدارة المرضى والأطباء والخدمات في واجهة واحدة",
          "مؤشرات فورية للحجوزات والإيرادات",
          "حل يتدرج من العيادات الصغيرة إلى الشبكات الكبيرة",
        ],
      },
    ],
  },
} as const;

export default function HowItWorks() {
  const {language} = useLanguage();
  const data = content[language];
  // Refs for scroll triggering
  const sectionRef = useRef(null)
  const isInView = useInView(sectionRef, { once: false, amount: 0.1 })
  const mainControls = useAnimation()

  useEffect(() => {
    if (isInView) {
      mainControls.start("visible")
    }
  }, [isInView, mainControls])

  return (
    <section id="how-it-works" className="relative py-24 overflow-hidden bg-gradient-to-b from-slate-50 via-white to-slate-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950" ref={sectionRef}>
      {/* Background elements - floating gradient particles */}
      <div className="absolute inset-0 pointer-events-none">
        {[...Array(20)].map((_, i) => (
          <motion.div
            key={i}
            className="absolute rounded-full bg-gradient-to-br from-primary/15 to-mintlify-blue/15 blur-xl"
            style={{
              width: Math.random() * 100 + 50,
              height: Math.random() * 100 + 50,
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
            }}
            animate={{
              x: [0, Math.random() * 100 - 50],
              y: [0, Math.random() * 100 - 50],
            }}
            transition={{
              duration: Math.random() * 20 + 20,
              repeat: Infinity,
              repeatType: "reverse",
              ease: "easeInOut",
            }}
          />
        ))}
      </div>

      {/* Grid overlay */}
      <div className="absolute inset-0 bg-grid-pattern opacity-[0.03]" 
        style={{
          backgroundImage: 'url("data:image/svg+xml,%3Csvg width=\'30\' height=\'30\' viewBox=\'0 0 30 30\' fill=\'none\' xmlns=\'http://www.w3.org/2000/svg\'%3E%3Cpath d=\'M1.5 0H0V1.5V30H1.5V1.5H30V0H1.5Z\' fill=\'white\'/%3E%3C/svg%3E")',
          backgroundSize: '30px 30px',
        }}
      />

      <div className="container relative px-4 md:px-8 z-10">
        {/* Header with animated underline */}
        <div className="max-w-3xl mx-auto text-center mb-16 md:mb-24">
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7 }}
          >
            <span className="inline-block px-4 py-1.5 text-xs font-medium text-primary dark:text-primary/80 bg-primary/5 dark:bg-primary/10 rounded-full backdrop-blur-sm mb-4">
              {data.badge}
            </span>
            <h2 className="text-4xl md:text-5xl font-bold mb-6 tracking-tight">
              {data.title}
            </h2>
            <p className="text-slate-600 dark:text-gray-300 text-lg max-w-2xl mx-auto">
              {data.description}
            </p>
            
            {/* Animated underline */}
            <div className="relative w-40 h-1 mx-auto mt-6">
              <motion.div
                className="absolute inset-0 bg-gradient-to-r from-primary to-mintlify-blue rounded-full"
                initial={{ width: 0 }}
                animate={{ width: '100%' }}
                transition={{ delay: 0.5, duration: 0.8, ease: "easeOut" }}
              />
            </div>
          </motion.div>
        </div>

        {/* Interactive Timeline */}
        <div className="relative max-w-6xl mx-auto">
          {/* Main vertical line for desktop */}
          <div className="hidden md:block absolute left-1/2 top-0 bottom-0 w-1 bg-gradient-to-b from-primary/70 via-mintlify-blue/70 to-mintlify-blue/80 rounded-full transform -translate-x-1/2" />
          
          {/* Steps container */}
          <div className="space-y-20 md:space-y-32">
            {data.steps.map((step, index) => (
              <motion.div
                key={index}
                variants={{
                  hidden: { opacity: 0 },
                  visible: { opacity: 1, transition: { duration: 0.8, delay: index * 0.2 } }
                }}
                initial="hidden"
                animate={mainControls}
                className={`flex flex-col ${index % 2 === 0 ? 'md:flex-row' : 'md:flex-row-reverse'} items-center gap-6 md:gap-12`}
              >
                {/* Step number without hover animation */}
                <div className="relative shrink-0 z-10">
                  <div className="w-16 h-16 md:w-20 md:h-20 bg-white dark:bg-gray-900 rounded-full border-2 border-primary/40 flex items-center justify-center shadow-[0_20px_45px_rgba(24,226,153,0.2)]">
                    <span className="text-2xl md:text-3xl font-bold bg-gradient-to-r from-primary to-mintlify-blue bg-clip-text text-transparent">
                      {step.number}
                    </span>
                  </div>
                  {/* Pulsing circle animation */}
                  <div className="absolute -inset-3 z-0">
                    <div className="absolute inset-0 rounded-full bg-primary/20 animate-ping opacity-50" />
                    <div className="absolute inset-0 rounded-full bg-gradient-to-r from-primary/20 to-mintlify-blue/20 blur-sm" />
                  </div>
                </div>

                {/* Content card without hover animation */}
                <div className="flex-1">
                  <div className="relative bg-white/95 dark:bg-gray-900/90 backdrop-blur-md rounded-xl overflow-hidden md:max-w-[90%]">
                    <div className="absolute inset-0 bg-gradient-to-br from-primary/15 via-transparent to-mintlify-blue/15 opacity-50" />
                    <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-primary to-mintlify-blue" />
                    
                    <div className="p-6 md:p-8 relative">
                      <div className="flex flex-col md:flex-row gap-6 md:gap-10 items-start">
                        <div className="flex-1">
                          <h3 className="text-2xl font-bold mb-3">{step.title}</h3>
                          <p className="text-slate-600 dark:text-gray-300">{step.description}</p>
                          
                          <ul className="mt-5 space-y-2">
                            {step.bullets?.map((item) => (
                              <li key={item} className="flex items-start gap-2">
                                <CheckCircle className="h-5 w-5 text-primary/80 mt-0.5 shrink-0" />
                                <span className="text-sm text-slate-600 dark:text-gray-300">{item}</span>
                              </li>
                            ))}
                          </ul>

                          <div className="mt-6">
                            <a
                              href="#faq"
                              className="inline-flex items-center text-sm font-medium text-primary/80 hover:text-primary transition-colors"
                            >
                              Learn more about this step <ChevronRight className="ml-1 h-4 w-4" />
                            </a>
                          </div>
                        </div>
                        
                        {/* Image without hover animation */}
                        <div className="relative shrink-0 md:w-1/2 aspect-[4/3] rounded-lg overflow-hidden">
                          <div className="absolute inset-0 bg-gradient-to-br from-primary/10 to-mintlify-blue/10 z-10" />
                          <Image
                            src={step.image || "/placeholder.svg"}
                            alt={step.title}
                            fill
                            className="object-cover"
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
        
        {/* CTA without hover animation */}
        <motion.div 
          className="mt-20 text-center"
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: isInView ? 1 : 0, y: isInView ? 0 : 30 }}
          transition={{ duration: 0.8, delay: 0.5 }}
          >
            <div className="relative inline-block">
              <div className="absolute -inset-1 bg-gradient-to-r from-primary to-mintlify-blue rounded-lg blur-md opacity-70" />
            <a 
              href="mailto:implementation@cmc.health" 
              className="relative inline-flex items-center gap-2 px-8 py-4 rounded-lg bg-mintlify-gradient text-white font-medium text-lg hover:opacity-90 transition-all shadow-[0_30px_60px_rgba(24,226,153,0.3)]"
            >
              {data.cta} <ArrowRight className="h-5 w-5" />
            </a>
          </div>
          
          <p className="mt-4 text-slate-600 dark:text-gray-400">
            {data.ctaSub}
          </p>
        </motion.div>
      </div>
    </section>
  )
}
