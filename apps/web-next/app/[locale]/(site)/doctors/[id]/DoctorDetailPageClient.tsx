'use client';

import { useEffect, useState } from 'react';
import Image from 'next/image';
import { useTranslations, useLocale } from 'next-intl';
import { Link } from '@/navigation';
import { api } from '@/lib/api';
import type { Doctor } from '@/lib/types';

// Fallback image for doctors without profile images
const defaultDoctorImage = 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?q=80&w=800&auto=format&fit=crop';

type DoctorDetailPageClientProps = {
  doctorId: number;
};

export default function DoctorDetailPageClient({ doctorId }: DoctorDetailPageClientProps) {
  const locale = useLocale();
  
  const [doctor, setDoctor] = useState<Doctor | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const t = useTranslations('doctors.detail');
  const navT = useTranslations('nav');
  const commonT = useTranslations('common');

  useEffect(() => {
    const fetchDoctor = async () => {
      try {
        if (isNaN(doctorId)) {
          setError(t('errors.invalidId'));
          return;
        }
        
        console.log('Fetching doctor with ID:', doctorId);
        const data = await api.getDoctor(doctorId, locale);
        console.log('Doctor data received:', data);
        setDoctor(data);
      } catch (err: any) {
        console.error('Error fetching doctor:', err);
        setError(err.message || t('errors.notFound'));
      } finally {
        setLoading(false);
      }
    };

    fetchDoctor();
  }, [doctorId, locale, t]);

  if (loading) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-20">
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-600 border-t-transparent dark:border-blue-400"></div>
              <p className="mt-4 text-slate-600 dark:text-slate-300 font-medium">{t('loading')}</p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (error || !doctor) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-20">
          <div className="p-6 rounded-2xl bg-red-50 dark:bg-red-950/50 border-2 border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 shadow-md">
            <div className="flex items-center gap-3">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="font-semibold">{error || t('errors.notFound')}</span>
            </div>
          </div>
          <div className="mt-6">
            <Link
              href="/doctors"
              className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 font-medium transition-colors"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
              {t('backToDoctors')}
            </Link>
          </div>
        </div>
      </main>
    );
  }

  const imageUrl = doctor.imageUrl || defaultDoctorImage;

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
      {/* Back Button */}
      <div className="bg-white dark:bg-slate-900/60 border-b border-slate-200 dark:border-slate-800/70 backdrop-blur transition-colors">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-4">
          <Link
            href="/doctors"
            className="inline-flex items-center gap-2 text-slate-600 hover:text-blue-600 dark:text-slate-300 dark:hover:text-blue-400 font-medium transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
            {t('backToDoctors')}
          </Link>
        </div>
      </div>

      {/* Doctor Profile Section */}
      <section className="py-16">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-12 items-start">
            {/* Doctor Image */}
            <div className="relative">
              <div className="relative overflow-hidden rounded-3xl border border-slate-100/60 dark:border-slate-800/60 shadow-2xl dark:shadow-blue-900/50 transition-colors">
                <Image
                  src={imageUrl}
                  alt={doctor.name}
                  width={800}
                  height={600}
                  sizes="(max-width: 1024px) 100vw, 50vw"
                  className="h-96 w-full object-cover lg:h-[500px]"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-slate-900/20 to-transparent" />
              </div>
            </div>

            {/* Doctor Information */}
            <div className="space-y-8">
              <div>
                <h1 className="text-4xl md:text-5xl font-bold text-slate-900 dark:text-slate-100 mb-4 transition-colors">
                  {doctor.name}
                </h1>
                
                {doctor.specialty && (
                  <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-gradient-to-r from-blue-100 to-cyan-100 dark:from-blue-950/50 dark:to-cyan-950/50 border border-blue-200 dark:border-blue-800 text-blue-700 dark:text-blue-300 font-semibold transition-colors">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
                    </svg>
                    {doctor.specialty}
                  </div>
                )}
              </div>

              {/* Bio */}
              {doctor.bio && (
                <div>
                  <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-100 mb-3 transition-colors">
                    {t('about')}
                  </h3>
                  <p className="text-slate-600 dark:text-slate-300 leading-relaxed transition-colors">
                    {doctor.bio}
                  </p>
                </div>
              )}

              {/* Services */}
              {doctor.services && doctor.services.length > 0 && (
                <div>
                  <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-100 mb-3 transition-colors">
                    {t('specializations')}
                  </h3>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {doctor.services.map((service) => (
                      <div
                        key={service.slug}
                        className="flex items-center gap-3 p-3 rounded-lg bg-gradient-to-r from-blue-50 to-cyan-50 dark:from-blue-950/30 dark:to-cyan-950/30 border border-blue-100 dark:border-blue-800/50 transition-colors"
                      >
                        <div className="w-8 h-8 rounded-full bg-gradient-to-r from-blue-600 to-cyan-600 dark:from-blue-500 dark:to-cyan-500 flex items-center justify-center flex-shrink-0">
                          <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                          </svg>
                        </div>
                        <div>
                          <h4 className="font-semibold text-slate-900 dark:text-slate-100 text-sm transition-colors">
                            {service.name}
                          </h4>
                          {service.summary && (
                            <p className="text-xs text-slate-600 dark:text-slate-400 mt-1 transition-colors">
                              {service.summary}
                            </p>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Languages */}
              {doctor.languages.length > 0 && (
                <div>
                  <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-100 mb-3 transition-colors">
                    {t('languages')}
                  </h3>
                  <div className="flex flex-wrap gap-2">
                    {doctor.languages.map((lang) => (
                      <span
                        key={lang}
                        className="inline-flex items-center gap-1 rounded-full bg-white dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 px-3 py-1 text-sm font-medium text-slate-700 dark:text-slate-300 transition-colors"
                      >
                        <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
                        </svg>
                        {lang === 'en' ? navT('language.english') : lang === 'ar' ? navT('language.arabic') : lang.toUpperCase()}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* Book Appointment Button */}
              <div className="pt-6">
                <Link
                  href="/#booking-section"
                  className="inline-flex items-center justify-center px-8 py-4 rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-semibold text-lg hover:from-blue-700 hover:to-cyan-700 transition-all shadow-lg hover:shadow-xl dark:shadow-blue-900/40 dark:hover:shadow-blue-900/60 transform hover:-translate-y-0.5"
                >
                  {t('bookAppointment', { name: doctor.name.split(' ').pop() ?? doctor.name })}
                  <svg className="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Professional Highlights Section */}
      <section className="py-16 bg-white dark:bg-slate-900/60 transition-colors">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-12">
            <h2 className="text-3xl md:text-4xl font-bold text-slate-900 dark:text-slate-100 mb-4 transition-colors">
              {t('highlights.title')}
            </h2>
            <p className="text-slate-600 dark:text-slate-300 text-lg transition-colors">
              {t('highlights.subtitle')}
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center p-8 rounded-2xl border border-slate-100/70 dark:border-slate-800/80 bg-gradient-to-br from-blue-50/80 to-cyan-50/80 dark:from-blue-950/30 dark:to-cyan-950/30 backdrop-blur transition-colors">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-blue-600 to-cyan-600 dark:from-blue-500 dark:to-cyan-500 flex items-center justify-center mx-auto mb-4 shadow-lg dark:shadow-blue-900/40">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
                </svg>
              </div>
              <h3 className="font-bold text-lg mb-2 text-slate-900 dark:text-slate-100 transition-colors">
                {t('highlights.items.certified.title')}
              </h3>
              <p className="text-slate-600 dark:text-slate-300 text-sm transition-colors">
                {t('highlights.items.certified.description')}
              </p>
            </div>

            <div className="text-center p-8 rounded-2xl border border-slate-100/70 dark:border-slate-800/80 bg-gradient-to-br from-blue-50/80 to-cyan-50/80 dark:from-blue-950/30 dark:to-cyan-950/30 backdrop-blur transition-colors">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-blue-600 to-cyan-600 dark:from-blue-500 dark:to-cyan-500 flex items-center justify-center mx-auto mb-4 shadow-lg dark:shadow-blue-900/40">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
              </div>
              <h3 className="font-bold text-lg mb-2 text-slate-900 dark:text-slate-100 transition-colors">
                {t('highlights.items.patientCare.title')}
              </h3>
              <p className="text-slate-600 dark:text-slate-300 text-sm transition-colors">
                {t('highlights.items.patientCare.description')}
              </p>
            </div>

            <div className="text-center p-8 rounded-2xl border border-slate-100/70 dark:border-slate-800/80 bg-gradient-to-br from-blue-50/80 to-cyan-50/80 dark:from-blue-950/30 dark:to-cyan-950/30 backdrop-blur transition-colors">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-blue-600 to-cyan-600 dark:from-blue-500 dark:to-cyan-500 flex items-center justify-center mx-auto mb-4 shadow-lg dark:shadow-blue-900/40">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <h3 className="font-bold text-lg mb-2 text-slate-900 dark:text-slate-100 transition-colors">
                {t('highlights.items.modernTech.title')}
              </h3>
              <p className="text-slate-600 dark:text-slate-300 text-sm transition-colors">
                {t('highlights.items.modernTech.description')}
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Call to Action */}
      <section className="py-16 bg-gradient-to-r from-blue-600 to-cyan-600 dark:from-blue-800/80 dark:via-blue-900/80 dark:to-slate-900 transition-colors">
        <div className="mx-auto max-w-7xl px-4 text-center md:px-6 lg:px-8">
          <h2 className="mb-6 text-4xl font-bold text-white md:text-5xl">
            {t('cta.title', { name: doctor.name.split(' ')[0] })}
          </h2>
          <p className="mx-auto mb-8 max-w-2xl text-xl text-blue-50">
            {t('cta.subtitle')}
          </p>
          <div className="flex flex-col items-center justify-center gap-4 sm:flex-row">
            <Link
              href="/#booking-section"
              className="inline-flex items-center justify-center rounded-xl bg-white/95 px-8 py-4 text-lg font-semibold text-blue-700 transition-all hover:bg-blue-50 shadow-lg hover:shadow-xl dark:bg-blue-100/15 dark:text-blue-100 dark:hover:bg-blue-100/25 dark:shadow-blue-900/50 dark:hover:shadow-blue-900/70"
            >
              {t('cta.scheduleAppointment')}
              <svg className="h-5 w-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </Link>
            <Link
              href="/doctors"
              className="inline-flex items-center justify-center rounded-xl border-2 border-white/90 px-8 py-4 text-lg font-semibold text-white transition-all hover:bg-white/15 dark:border-white/70"
            >
              {t('cta.viewAllDoctors')}
            </Link>
          </div>
        </div>
      </section>
    </main>
  );
}
