import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Mail } from "lucide-react"
import {useLanguage} from '@/contexts/LanguageContext';

const COPY = {
  en: {
    label: 'Newsletter',
    title: 'Stay Updated with SaasPro',
    description: 'Subscribe to our newsletter for the latest updates, tips, and exclusive offers delivered directly to your inbox.',
    placeholder: 'Enter your email address',
    cta: 'Subscribe Now',
    privacy: 'We respect your privacy. Unsubscribe at any time. No spam, ever.',
  },
  ar: {
    label: 'النشرة البريدية',
    title: 'تابع تحديثات SaasPro',
    description: 'اشترك في النشرة لتصلك آخر التحديثات والنصائح والعروض الحصرية مباشرة إلى بريدك.',
    placeholder: 'أدخل بريدك الإلكتروني',
    cta: 'اشترك الآن',
    privacy: 'نحترم خصوصيتك. يمكنك الإلغاء في أي وقت. لا رسائل مزعجة.',
  },
} as const;

export default function Newsletter() {
  const {language} = useLanguage();
  const data = COPY[language];
  return (
    <section className="relative py-20 overflow-hidden">
      {/* Background elements */}
      <div className="absolute inset-0 bg-gradient-to-b from-white to-slate-50 dark:from-gray-900 dark:to-gray-950"></div>

      {/* Dot pattern background */}
      <div className="absolute inset-0 opacity-20">
        <div className="absolute inset-0" style={{
          backgroundImage: `radial-gradient(circle at 1px 1px, rgb(124, 58, 237, 0.15) 2px, transparent 0)`,
          backgroundSize: '24px 24px'
        }}></div>
      </div>

      <div className="container relative px-4 md:px-8">
        <div className="max-w-4xl mx-auto">
          <div className="backdrop-blur-sm bg-white/90 dark:bg-gray-900/80 border border-slate-200 dark:border-gray-800 rounded-2xl p-8 md:p-12 relative overflow-hidden shadow-xl">
            {/* Decorative elements */}
            <div className="absolute -top-24 -right-24 w-64 h-64 bg-primary/15 rounded-full blur-3xl"></div>
            <div className="absolute -bottom-24 -left-24 w-64 h-64 bg-mintlify-blue/15 rounded-full blur-3xl"></div>
            
            {/* Top gradient border */}
            <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-primary to-mintlify-blue"></div>

            <div className="relative text-center mb-10">
              <span className="inline-block px-3 py-1 text-xs font-medium text-primary dark:text-primary/80 bg-primary/5 dark:bg-primary/15 rounded-full mb-3">{data.label}</span>
              <h2 className="text-2xl md:text-3xl font-bold mb-4 text-slate-900 dark:bg-gradient-to-r dark:from-white dark:to-gray-300 dark:bg-clip-text dark:text-transparent">
                {data.title}
              </h2>
              <p className="text-slate-600 dark:text-gray-300 max-w-lg mx-auto">
                {data.description}
              </p>
            </div>

            <form className="flex flex-col sm:flex-row gap-4 max-w-xl mx-auto">
              <div className="relative flex-grow">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-slate-500 dark:text-gray-400" />
                </div>
                <Input
                  type="email"
                  placeholder={data.placeholder}
                  className="pl-10 bg-white border-slate-300 text-slate-900 focus:border-primary dark:bg-gray-800/50 dark:border-gray-700 dark:text-white h-12 rounded-lg"
                />
              </div>
              <Button className="h-12 px-6 rounded-lg font-medium bg-mintlify-gradient hover:opacity-90 transition-all duration-300">
                {data.cta}
              </Button>
            </form>

            <p className="text-xs text-slate-600 dark:text-gray-400 text-center mt-6">
              {data.privacy}
            </p>
          </div>
        </div>
      </div>
    </section>
  )
}
