import Image from "next/image"
import Link from "next/link"
import { ArrowRight } from "lucide-react"
import { Button } from "@/components/ui/button"
import {useLanguage} from '@/contexts/LanguageContext';

const blogCopy = {
  en: {
    badge: 'Latest Articles',
    title: 'From Our Blog',
    cta: 'View all playbooks',
    articles: [
      {
        title: "Designing bilingual patient portals that convert",
        excerpt: "Lessons from rolling out English + Arabic journeys without maintaining two separate sites.",
        image: "https://images.unsplash.com/photo-1466786784937-3e682c802b14?q=80&w=2070&auto=format&fit=crop",
        category: "Patient Experience",
        date: "Mar 12, 2025",
        readTime: "7 min read",
      },
      {
        title: "What clinic operators should log for compliance",
        excerpt: "A practical audit checklist for treatment plans, staff actions, and financial transactions.",
        image: "https://images.unsplash.com/photo-1521791055366-0d553872125f?q=80&w=2070&auto=format&fit=crop",
        category: "Operations",
        date: "Mar 2, 2025",
        readTime: "6 min read",
      },
      {
        title: "Automating reminders without annoying patients",
        excerpt: "How to mix SMS, WhatsApp, and email to cut no-shows by 34% at Shifa Health Network.",
        image: "https://images.unsplash.com/photo-1526256262350-7da7584cf5eb?q=80&w=2070&auto=format&fit=crop",
        category: "Automation",
        date: "Feb 18, 2025",
        readTime: "5 min read",
      },
    ],
  },
  ar: {
    badge: 'أحدث المقالات',
    title: 'من مدونتنا',
    cta: 'استعرض كل الأدلة',
    articles: [
      {
        title: "تصميم بوابات مرضى ثنائية اللغة تحقق التحويل",
        excerpt: "دروس من إطلاق تجارب عربية وإنجليزية دون الحاجة لإدارة موقعين منفصلين.",
        image: "https://images.unsplash.com/photo-1466786784937-3e682c802b14?q=80&w=2070&auto=format&fit=crop",
        category: "تجربة المرضى",
        date: "12 مارس 2025",
        readTime: "7 دقائق قراءة",
      },
      {
        title: "ما الذي يجب على مديري العيادات توثيقه للامتثال؟",
        excerpt: "قائمة تدقيق عملية لتوثيق الخطط العلاجية وإجراءات الموظفين والمعاملات المالية.",
        image: "https://images.unsplash.com/photo-1521791055366-0d553872125f?q=80&w=2070&auto=format&fit=crop",
        category: "العمليات",
        date: "2 مارس 2025",
        readTime: "6 دقائق قراءة",
      },
      {
        title: "أتمتة التذكيرات دون إزعاج المرضى",
        excerpt: "كيف نمزج بين الرسائل النصية وواتساب والبريد لتقليل التغيب بنسبة 34٪ في شبكة شفاء.",
        image: "https://images.unsplash.com/photo-1526256262350-7da7584cf5eb?q=80&w=2070&auto=format&fit=crop",
        category: "الأتمتة",
        date: "18 فبراير 2025",
        readTime: "5 دقائق قراءة",
      },
    ],
  },
} as const;

export default function BlogPreview() {
  const {language} = useLanguage();
  const data = blogCopy[language];

  return (
    <section id="blog" className="relative py-20 md:py-32 overflow-hidden">
      {/* Background elements */}
      <div className="absolute inset-0 bg-gradient-to-b from-white to-slate-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950"></div>

      <div className="container relative px-4 md:px-8">
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-12">
          <div>
            <p className="text-primary dark:text-primary/80 font-medium mb-2">{data.badge}</p>
            <h2 className="text-3xl md:text-4xl font-bold text-slate-900 dark:text-white">{data.title}</h2>
          </div>
          <div className="mt-4 md:mt-0">
            <Button variant="link" className="text-primary dark:text-primary/80 hover:text-primary/70 p-0 h-auto flex items-center gap-1" asChild>
              <a href="https://github.com/MohamedDjoudir/landing-page-template-2/tree/main/docs" target="_blank" rel="noreferrer">
                {data.cta} <ArrowRight className="h-4 w-4 ml-1" />
              </a>
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {data.articles.map((article, index) => (
            <Link href="#" key={index} className="group">
              <div className="bg-white dark:bg-gray-900 border border-slate-200 dark:border-gray-800 rounded-xl overflow-hidden transition-all duration-300 hover:border-primary/30 hover:shadow-[0_30px_55px_rgba(24,226,153,0.15)]">
                <div className="relative h-48 overflow-hidden">
                  <Image
                    src={article.image || "/placeholder.svg"}
                    alt={article.title}
                    fill
                    className="object-cover transition-transform duration-500 group-hover:scale-105"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-gray-900 to-transparent opacity-60"></div>
                  <div className="absolute top-4 left-4 bg-mintlify-gradient text-white text-xs font-medium px-2 py-1 rounded">
                    {article.category}
                  </div>
                </div>
                <div className="p-6">
                <div className="flex items-center text-sm text-slate-600 dark:text-gray-400 mb-3">
                  <span>{article.date}</span>
                    <span className="mx-2">•</span>
                    <span>{article.readTime}</span>
                  </div>
                  <h3 className="text-xl font-bold mb-2 group-hover:text-primary transition-colors">
                    {article.title}
                  </h3>
                  <p className="text-slate-600 dark:text-gray-400 text-sm line-clamp-2">{article.excerpt}</p>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </section>
  )
}
