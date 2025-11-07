'use client';

import {Link} from '@/navigation';
import {useEffect, useState} from 'react';
import Image from 'next/image';
import {useTranslations, useLocale} from 'next-intl';
import {api} from '@/lib/api';
import type {Doctor} from '@/lib/types';

// Fallback image for doctors without profile images
const defaultDoctorImage = 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?q=80&w=800&auto=format&fit=crop';

export default function DoctorsPageClient() {
  const locale = useLocale();
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const t = useTranslations('doctors.list');
  const navT = useTranslations('nav');

  useEffect(() => {
    const fetchDoctors = async () => {
      try {
        console.log('Fetching doctors for locale:', locale);
        const data = await api.getDoctors(locale);
        console.log('Doctors data received:', data);
        setDoctors(data);
      } catch (err: any) {
        console.error('Error fetching doctors:', err);
        setError(err.message || t('delete.unable'));
      } finally {
        setLoading(false);
      }
    };

    fetchDoctors();
  }, [locale, t]);

  if (loading) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-20">
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-600 border-t-transparent"></div>
              <p className="mt-4 text-slate-600 dark:text-slate-300 font-medium">{t('status.loading')}</p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (error) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-20">
          <div className="p-6 rounded-2xl bg-red-50 dark:bg-red-950/50 border-2 border-red-200 dark:border-red-800 text-red-700 dark:text-red-300">
            <div className="flex items-center gap-3">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="font-semibold">{error}</span>
            </div>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
      {/* Header Section */}
      <section className="py-16 bg-gradient-to-r from-blue-600 to-cyan-600 dark:from-blue-800/80 dark:via-blue-900/80 dark:to-slate-900">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto">
            <h1 className="text-4xl md:text-5xl font-bold text-white">{t('hero.title')}</h1>
            <p className="text-blue-50 mt-4 text-lg">
              {t('hero.subtitle')}
            </p>
          </div>
        </div>
      </section>

      {/* Doctors Grid */}
      <section className="py-16">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          {doctors.length === 0 ? (
            <div className="max-w-2xl mx-auto p-12 rounded-2xl bg-white dark:bg-slate-800 border-2 border-dashed border-slate-200 dark:border-slate-700 text-center">
              <svg className="w-16 h-16 mx-auto text-slate-300 dark:text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              <p className="text-slate-600 dark:text-slate-300 mt-4 text-lg font-medium">{t('status.emptyTitle')}</p>
              <p className="text-slate-500 dark:text-slate-400 mt-2">{t('status.emptySubtitle')}</p>
            </div>
          ) : (
            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-8">
              {doctors.map((doctor) => {
                const imageUrl = doctor.imageUrl || defaultDoctorImage;

                return (
                  <div
                    key={doctor.id}
                    className="group rounded-2xl overflow-hidden border border-slate-100/70 dark:border-slate-800/80 bg-white/90 dark:bg-slate-900/70 backdrop-blur transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_25px_60px_-25px_rgba(59,130,246,0.45)] dark:hover:shadow-[0_25px_60px_-25px_rgba(37,99,235,0.45)] hover:border-blue-200 dark:hover:border-blue-600"
                  >
                    <div className="relative overflow-hidden">
                      <Image
                        src={imageUrl}
                        alt={doctor.name}
                        width={800}
                        height={600}
                        sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
                        className="h-64 w-full object-cover transition-transform duration-300 group-hover:scale-105"
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-slate-900/50 to-transparent"></div>
                    </div>
                    <div className="p-6">
                      <Link href={`/doctors/${doctor.id}`}>
                        <h3 className="text-xl font-bold text-slate-900 dark:text-slate-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors cursor-pointer">{doctor.name}</h3>
                      </Link>
                      <div className="mt-2 inline-flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                        <svg className="w-4 h-4 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
                        </svg>
                        <span className="font-medium">{doctor.specialty ?? t('labels.specialtyNotSet')}</span>
                      </div>
                      {doctor.languages.length > 0 && (
                        <div className="mt-4 flex flex-wrap gap-2">
                          {doctor.languages.map((lang) => (
                            <span
                              key={lang}
                              className="inline-flex items-center gap-1 rounded-full bg-gradient-to-br from-blue-50 to-cyan-50 dark:from-blue-950/50 dark:to-cyan-950/50 border border-blue-200 dark:border-blue-800 px-3 py-1 text-xs font-semibold text-blue-700 dark:text-blue-300"
                            >
                              <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
                              </svg>
                              {lang === 'en' ? navT('language.english') : lang === 'ar' ? navT('language.arabic') : lang.toUpperCase()}
                            </span>
                          ))}
                        </div>
                      )}
                      {doctor.languages.length === 0 && (
                        <p className="mt-4 text-sm text-slate-500 dark:text-slate-400">{t('labels.languagesPending')}</p>
                      )}
                      <div className="mt-6 flex flex-col gap-3">
                        <Link
                          href={`/doctors/${doctor.id}`}
                          className="w-full inline-flex items-center justify-center rounded-xl border-2 border-blue-200 dark:border-blue-700 bg-white/90 dark:bg-slate-800/60 text-blue-700 dark:text-blue-300 px-6 py-3 font-semibold hover:border-blue-300 dark:hover:border-blue-600 hover:bg-blue-50 dark:hover:bg-slate-700/60 transition-all shadow hover:shadow-lg backdrop-blur"
                        >
                          {t('actions.viewProfile')}
                          <svg className="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                          </svg>
                        </Link>
                        <Link
                          href="/#booking-section"
                          className="w-full inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 text-white px-6 py-3 font-semibold hover:from-blue-700 hover:to-cyan-700 transition-all shadow hover:shadow-lg"
                        >
                          {t('actions.bookWith', { name: doctor.name.split(' ').pop() ?? doctor.name })}
                          <svg className="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                          </svg>
                        </Link>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          {/* Why Choose Our Doctors Section */}
          <div className="mt-20">
            <div className="text-center max-w-3xl mx-auto mb-12">
              <h2 className="text-3xl md:text-4xl font-bold text-slate-900 dark:text-slate-100">{t('featuresSection.title')}</h2>
              <p className="text-slate-600 dark:text-slate-300 mt-4 text-lg">
                {t('featuresSection.subtitle')}
              </p>
            </div>

            <div className="grid md:grid-cols-3 gap-8">
              <div className="text-center p-8 rounded-2xl border border-slate-100/70 dark:border-slate-800/80 bg-white/90 dark:bg-slate-900/70 backdrop-blur">
                <div className="w-16 h-16 rounded-full bg-gradient-to-br from-blue-600 to-cyan-600 dark:from-blue-500 dark:to-cyan-500 flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
                  </svg>
                </div>
                <h3 className="font-bold text-lg mb-2">{t('featuresSection.items.certified.title')}</h3>
                <p className="text-slate-600 dark:text-slate-300 text-sm">{t('featuresSection.items.certified.description')}</p>
              </div>

              <div className="text-center p-8 rounded-2xl border border-slate-100/70 dark:border-slate-800/80 bg-white/90 dark:bg-slate-900/70 backdrop-blur">
                <div className="w-16 h-16 rounded-full bg-gradient-to-br from-blue-600 to-cyan-600 dark:from-blue-500 dark:to-cyan-500 flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                  </svg>
                </div>
                <h3 className="font-bold text-lg mb-2">{t('featuresSection.items.patientCare.title')}</h3>
                <p className="text-slate-600 dark:text-slate-300 text-sm">{t('featuresSection.items.patientCare.description')}</p>
              </div>

              <div className="text-center p-8 rounded-2xl border border-slate-100/70 dark:border-slate-800/80 bg-white/90 dark:bg-slate-900/70 backdrop-blur">
                <div className="w-16 h-16 rounded-full bg-gradient-to-br from-blue-600 to-cyan-600 dark:from-blue-500 dark:to-cyan-500 flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                <h3 className="font-bold text-lg mb-2">{t('featuresSection.items.modernTech.title')}</h3>
                <p className="text-slate-600 dark:text-slate-300 text-sm">{t('featuresSection.items.modernTech.description')}</p>
              </div>
            </div>
          </div>

          {/* CTA Section */}
          <div className="mt-16 text-center p-12 rounded-2xl bg-gradient-to-br from-blue-50 to-cyan-50 dark:from-blue-900/50 dark:via-slate-900/60 dark:to-blue-950/50 border border-blue-100/70 dark:border-blue-900/60 shadow-xl shadow-blue-100/40 dark:shadow-blue-900/40 backdrop-blur">
            <h2 className="text-3xl font-bold text-slate-900 dark:text-slate-100 mb-4">{t('cta.title')}</h2>
            <p className="text-slate-600 dark:text-slate-300 text-lg mb-8 max-w-2xl mx-auto">
              {t('cta.subtitle')}
            </p>
            <Link
              href="/#booking-section"
              className="inline-flex items-center justify-center px-8 py-4 rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-semibold text-lg hover:from-blue-700 hover:to-cyan-700 transition-all shadow-lg hover:shadow-xl dark:shadow-blue-900/40 dark:hover:shadow-blue-900/60 transform hover:-translate-y-0.5"
            >
              {t('cta.button')}
              <svg className="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </Link>
          </div>
        </div>
      </section>
    </main>
  );
}
