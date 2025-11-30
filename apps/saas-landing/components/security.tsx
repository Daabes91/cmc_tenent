"use client"

import { motion } from "framer-motion"
import { Shield, Lock, Database, Clock, UserCheck, FileCheck, CheckCircle2 } from "lucide-react"
import { useLanguage } from '@/contexts/LanguageContext'
import { healthcareCopy } from '@/lib/content/healthcare-copy'
import { Button } from "@/components/ui/button"

export default function Security() {
  const { language } = useLanguage();
  const securityData = healthcareCopy[language].security;

  // Icon mapping for trust indicators
  const iconMap: Record<number, any> = {
    0: Lock,
    1: Shield,
    2: Database,
    3: Clock,
    4: UserCheck,
    5: FileCheck,
  };

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
      },
    },
  }

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        duration: 0.5,
      },
    },
  }

  const badgeVariants = {
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
      <div className="absolute inset-0 bg-gradient-to-b from-slate-50 to-white dark:from-gray-950 dark:to-gray-900"></div>

      {/* Decorative elements */}
      <div className="absolute top-0 left-0 w-full h-full overflow-hidden">
        <div className="absolute top-20 right-20 w-96 h-96 bg-primary/10 rounded-full blur-3xl"></div>
        <div className="absolute bottom-20 left-20 w-96 h-96 bg-mintlify-blue/10 rounded-full blur-3xl"></div>
      </div>

      <div className="container relative px-4 md:px-8">
        {/* Header */}
        <div className="max-w-3xl mx-auto text-center mb-16">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
          >
            <div className="inline-flex items-center justify-center p-2 bg-primary/10 rounded-full mb-4">
              <Shield className="w-6 h-6 text-primary" aria-hidden="true" />
            </div>
            <h2 className="text-3xl md:text-4xl font-bold mb-6 text-slate-900 dark:text-white">
              {securityData.title}
            </h2>
            <p className="text-slate-600 dark:text-gray-400 text-lg">
              {securityData.description}
            </p>
          </motion.div>
        </div>

        {/* Security Badges */}
        <motion.div
          className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-16 max-w-4xl mx-auto"
          variants={containerVariants}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: "-100px" }}
        >
          {securityData.badges.map((badge, index) => (
            <motion.div
              key={index}
              variants={badgeVariants}
              className="group"
            >
              <div className="bg-white dark:bg-gray-900 border-2 border-primary/20 rounded-xl p-6 text-center transition-all duration-300 hover:border-primary/40 hover:shadow-lg hover:shadow-primary/10">
                <div className="inline-flex items-center justify-center w-12 h-12 bg-primary/10 rounded-full mb-3 group-hover:scale-110 transition-transform duration-300">
                  <Shield className="w-6 h-6 text-primary" aria-hidden="true" />
                </div>
                <p className="font-semibold text-slate-900 dark:text-white text-sm">
                  {badge}
                </p>
              </div>
            </motion.div>
          ))}
        </motion.div>

        {/* Trust Indicators */}
        <motion.div
          className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto"
          variants={containerVariants}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: "-100px" }}
        >
          {securityData.trustIndicators.map((indicator, index) => {
            const Icon = iconMap[index] || CheckCircle2;
            return (
              <motion.div
                key={index}
                variants={itemVariants}
                className="group"
              >
                <div className="bg-white dark:bg-gray-900 border border-slate-200 dark:border-gray-800 rounded-xl p-6 h-full transition-all duration-300 hover:border-primary/40 hover:shadow-md">
                  <div className="flex items-start gap-4">
                    <div className="flex-shrink-0">
                      <div className="inline-flex items-center justify-center w-10 h-10 bg-primary/10 rounded-lg group-hover:bg-primary/20 transition-colors duration-300">
                        <Icon className="w-5 h-5 text-primary" aria-hidden="true" />
                      </div>
                    </div>
                    <div className="flex-1">
                      <p className="text-slate-700 dark:text-gray-300 font-medium">
                        {indicator}
                      </p>
                    </div>
                  </div>
                </div>
              </motion.div>
            );
          })}
        </motion.div>

        {/* CTA Section */}
        <motion.div
          className="mt-16 text-center"
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5, delay: 0.3 }}
        >
          <div className="inline-flex flex-col sm:flex-row gap-4">
            <Button
              size="lg"
              className="h-12 px-8 text-base font-semibold bg-primary hover:bg-primary/90 focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:outline-none transition-all"
              asChild
            >
              <a href="/privacy-policy">
                {language === 'ar' ? 'سياسة الخصوصية' : 'Privacy Policy'}
              </a>
            </Button>
            <Button
              size="lg"
              variant="outline"
              className="h-12 px-8 text-base font-semibold border-primary/30 hover:bg-primary/10 focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:outline-none transition-all"
              asChild
            >
              <a href="/terms-of-service">
                {language === 'ar' ? 'شروط الخدمة' : 'Terms of Service'}
              </a>
            </Button>
          </div>
        </motion.div>
      </div>
    </section>
  )
}
