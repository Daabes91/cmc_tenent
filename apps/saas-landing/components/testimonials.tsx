'use client';

import Image from "next/image"
import { healthcareCopy } from "@/lib/content/healthcare-copy"
import { useLanguage } from "@/contexts/LanguageContext"
import SectionCTA from './SectionCTA'

export default function Testimonials() {
  const { language } = useLanguage();
  // Use healthcare-specific testimonials from configuration
  const { title, subtitle, items: testimonials } = healthcareCopy[language].testimonials

  return (
    <section id="testimonials" className="relative py-20 md:py-32">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-slate-50 via-white to-slate-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950"></div>

      <div className="container relative px-4 md:px-8">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="mb-4 text-3xl font-bold tracking-tight md:text-4xl">{title}</h2>
          <p className="mb-16 text-lg text-slate-600 dark:text-gray-400">
            {subtitle}
          </p>
        </div>
        <div className="grid gap-8 md:grid-cols-3">
          {testimonials.map((testimonial, index) => (
            <div
              key={index}
              className="flex flex-col rounded-xl border border-slate-200 bg-white/90 dark:border-gray-800 dark:bg-gray-900/50 p-6 backdrop-blur-sm transition-all hover:shadow-lg hover:border-primary/30"
            >
              <svg className="mb-4 h-8 w-8 text-primary/70" fill="currentColor" viewBox="0 0 24 24">
                <path d="M14.017 21v-7.391c0-5.704 3.731-9.57 8.983-10.609l.995 2.151c-2.432.917-3.995 3.638-3.995 5.849h4v10h-9.983zm-14.017 0v-7.391c0-5.704 3.748-9.57 9-10.609l.996 2.151c-2.433.917-3.996 3.638-3.996 5.849h3.983v10h-9.983z" />
              </svg>
              <p className="mb-6 flex-1 text-slate-700 dark:text-gray-300">{testimonial.quote}</p>
              
              {/* Metric badge - displayed if available */}
              {testimonial.metric && (
                <div className="mb-4 inline-flex items-center rounded-full bg-primary/10 px-3 py-1 text-sm font-medium text-primary">
                  <svg className="mr-1.5 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                  </svg>
                  {testimonial.metric}
                </div>
              )}
              
              <div className="flex items-center">
                <div className="relative mr-4 h-12 w-12 flex-shrink-0">
                  <Image
                    src={testimonial.avatar || "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?q=80&w=1974&auto=format&fit=crop&ixlib=rb-4.0.3"}
                    alt={testimonial.name}
                    width={48}
                    height={48}
                    className="rounded-full object-cover"
                  />
                </div>
                <div>
                  <p className="font-bold text-slate-900 dark:text-white">{testimonial.name}</p>
                  <p className="text-sm text-slate-600 dark:text-gray-400">{testimonial.role}</p>
                  <p className="text-xs text-slate-500 dark:text-gray-500">{testimonial.clinicType}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* CTA after testimonials section */}
      <SectionCTA variant="testimonials" />
    </section>
  )
}
