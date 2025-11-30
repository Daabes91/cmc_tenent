"use client"

import Image from "next/image"
import { motion } from "framer-motion"
import {useLanguage} from '@/contexts/LanguageContext';

const COPY = {
  en: {
    badge: 'Trusted by regional networks and boutique clinics',
    title: 'Clinics rely on CMC Platform to run day-to-day care',
    stats: [
      { value: "35+", label: "Clinic brands onboarded" },
      { value: "3.2M+", label: "Patient visits scheduled" },
      { value: "2 languages", label: "English & Arabic out-of-box" },
      { value: "99.98%", label: "Uptime across clinics" },
    ],
  },
  ar: {
    badge: 'موثوق من الشبكات الإقليمية والعيادات المتخصصة',
    title: 'العيادات تعتمد منصة CMC في تشغيل يومها',
    stats: [
      { value: "35+", label: "علامة طبية تستخدم المنصة" },
      { value: "3.2M+", label: "زيارة مجدولة عبر النظام" },
      { value: "لغتان", label: "الإنجليزية والعربية جاهزتان" },
      { value: "99.98%", label: "توفر عبر كل العيادات" },
    ],
  },
} as const;

export default function SocialProof() {
  const {language} = useLanguage();
  const statCopy = COPY[language];
  const companies = [
    { name: "Qadri Dental Group", logo: "/placeholder-logo.svg" },
    { name: "Shifa Health Network", logo: "/placeholder-logo.svg" },
    { name: "Zen Orthopedics", logo: "/placeholder-logo.svg" },
    { name: "Nova Smiles Collective", logo: "/placeholder-logo.svg" },
    { name: "MediPlus Primary Care", logo: "/placeholder-logo.svg" },
    { name: "Bloom Pediatrics", logo: "/placeholder-logo.svg" },
  ]

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

  return (
    <section className="relative py-16 overflow-hidden">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-white to-slate-50 dark:from-gray-900 dark:to-gray-950"></div>

      {/* Decorative elements */}
      <div className="absolute inset-0 opacity-10">
        <div className="absolute top-10 left-10 w-72 h-72 bg-mintlify-mint rounded-full filter blur-[100px]"></div>
        <div className="absolute bottom-10 right-10 w-72 h-72 bg-mintlify-blue rounded-full filter blur-[100px]"></div>
      </div>

      <div className="container relative px-4 md:px-8">
        <div className="text-center mb-12">
          <p className="text-lg text-primary dark:text-primary/80 font-medium mb-2">{statCopy.badge}</p>
          <h2 className="text-2xl md:text-3xl font-bold text-slate-900 dark:text-white">{statCopy.title}</h2>
        </div>

        {/* Company logos */}
        <motion.div
          className="flex flex-wrap justify-center items-center gap-8 mb-16"
          variants={containerVariants}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: "-100px" }}
        >
          {companies.map((company, index) => (
            <motion.div
              key={index}
              className="transition-all duration-300"
              variants={itemVariants}
            >
              <div className="bg-white dark:bg-gray-800 rounded-lg p-4 w-[80px] h-[40px] flex items-center justify-center shadow-sm dark:shadow-none">
                <Image
                  src={company.logo || "/placeholder.svg"}
                  alt={company.name}
                  width={50}
                  height={40}
                  className="w-auto h-auto brightness-0 invert"
                />
              </div>
            </motion.div>
          ))}
        </motion.div>

        {/* Stats */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 md:gap-12">
          {statCopy.stats.map((stat, index) => (
            <div key={index} className="text-center">
              <div className="relative">
                <div className="absolute -inset-1 rounded-lg bg-mintlify-gradient opacity-30 blur-sm"></div>
                <div className="relative bg-white rounded-lg p-6 border border-slate-200 shadow-sm dark:bg-gray-900 dark:border-gray-800">
                  <div className="text-3xl md:text-4xl font-bold bg-gradient-to-r from-primary to-mintlify-blue bg-clip-text text-transparent mb-2">
                    {stat.value}
                  </div>
                  <p className="text-slate-600 dark:text-gray-400">{stat.label}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
