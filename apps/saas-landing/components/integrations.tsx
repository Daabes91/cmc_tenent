"use client"

import Image from "next/image"
import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import { ArrowRight } from "lucide-react"
import { API_DOCS_URL } from '@/lib/constants'
import { useLanguage } from '@/contexts/LanguageContext'
import { healthcareCopy } from '@/lib/content/healthcare-copy'
import { useAnalytics } from '@/hooks/use-analytics'
import { withBasePath } from '@/lib/base-path'

export default function Integrations() {
  const { language } = useLanguage();
  const { trackCTA } = useAnalytics();
  const integrationsData = healthcareCopy[language].integrations;
  
  // Map healthcare integrations with fallback logos
  const integrations = integrationsData.items.map(item => {
    const logo =
      item.logo?.startsWith('/')
        ? withBasePath(item.logo)
        : item.logo || `https://cdn.simpleicons.org/${item.name.toLowerCase().replace(/\s+/g, '')}`;

    return {
      name: item.name,
      category: item.category,
      description: item.description,
      logo,
    };
  });


  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.05,
      },
    },
  }

  const itemVariants = {
    hidden: { opacity: 0, scale: 0.8 },
    visible: {
      opacity: 1,
      scale: 1,
      transition: {
        duration: 0.4,
      },
    },
  }

  return (
    <section className="relative py-20 md:py-32 overflow-hidden">
      {/* Background elements */}
      <div className="absolute inset-0 bg-gradient-to-b from-white to-slate-50 dark:from-gray-900 dark:to-gray-950"></div>

      {/* Decorative circles */}
      <div className="absolute top-0 left-0 w-full h-full overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-mintlify-blue/20 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-primary/20 rounded-full blur-3xl"></div>
      </div>

      <div className="container relative px-4 md:px-8">
        <div className="max-w-3xl mx-auto text-center mb-16">
          <p className="text-primary dark:text-primary/80 font-medium mb-2">
            {language === 'ar' ? 'اتصالات سلسة' : 'Seamless Connections'}
          </p>
          <h2 className="text-3xl md:text-4xl font-bold mb-6 text-slate-900 dark:text-white">
            {integrationsData.title}
          </h2>
          <p className="text-slate-600 dark:text-gray-400 text-lg">
            {integrationsData.subtitle}
          </p>
        </div>

        <motion.div
          className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-6"
          variants={containerVariants}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: "-100px" }}
        >
          {integrations.map((integration, index) => (
            <motion.div key={index} className="group" variants={itemVariants}>
              <div className="bg-white border border-slate-200 rounded-xl p-6 h-full flex flex-col items-center justify-center text-center transition-all duration-300 hover:border-primary/40 hover:bg-slate-50 dark:bg-gray-900 dark:border-gray-800 dark:hover:bg-gray-800">
                <div className="relative mb-4 w-16 h-16 flex items-center justify-center">
                  <div className="absolute inset-0 bg-gradient-to-r from-primary/20 to-mintlify-blue/20 rounded-full blur-md group-hover:opacity-100 opacity-0 transition-opacity duration-300"></div>
                  <div className="bg-slate-100 dark:bg-gray-800 rounded-full p-2 w-12 h-12 flex items-center justify-center">
                    <Image
                      src={integration.logo || withBasePath("/placeholder.svg")}
                      alt={integration.name}
                      width={40}
                      height={40}
                      className="relative z-10 w-8 h-8 object-contain brightness-0 invert"
                    />
                  </div>
                </div>
                <h3 className="font-medium mb-1 text-slate-900 dark:text-white">{integration.name}</h3>
                <p className="text-xs text-slate-500 dark:text-gray-500">{integration.description}</p>
              </div>
            </motion.div>
          ))}
        </motion.div>

        <div className="mt-12 text-center">
          <Button
            size="lg"
            variant="outline"
            className="h-12 px-8 text-base font-semibold border-primary/30 hover:bg-primary/10 focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:outline-none transition-all"
            asChild
          >
            <a
              href={API_DOCS_URL}
              target="_blank"
              rel="noreferrer"
              onClick={() => trackCTA(integrationsData.ctaText, 'integrations_section')}
            >
              {integrationsData.ctaText}
              <ArrowRight className="ml-2 h-5 w-5" aria-hidden="true" />
            </a>
          </Button>
        </div>
      </div>
    </section>
  )
}
