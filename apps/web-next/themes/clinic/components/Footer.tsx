'use client';

import {Link} from '@/navigation';
import {useEffect, useState} from 'react';
import {useLocale, useTranslations} from 'next-intl';
import {api} from '@/lib/api';
import type {ClinicSettings} from '@/lib/types';
import {getBookingSectionPath} from '@/utils/basePath';

export function Footer() {
  const currentYear = new Date().getFullYear();
  const [settings, setSettings] = useState<ClinicSettings | null>(null);
  const [loading, setLoading] = useState(true);
  const locale = useLocale();
  const t = useTranslations('footer');
  const nav = useTranslations('nav');
  const common = useTranslations('common');
  const bookingSectionPath = getBookingSectionPath(locale);

  useEffect(() => {
    const loadSettings = async () => {
      try {
        const data = await api.getClinicSettings();
        setSettings(data);
      } catch (error) {
        console.error('Failed to load clinic settings:', error);
      } finally {
        setLoading(false);
      }
    };
    loadSettings();
  }, []);

  return (
    <footer className="mt-auto bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-900 dark:to-slate-950 border-t border-slate-200 dark:border-slate-800">
      <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-12 lg:py-16">
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8 lg:gap-12">
          {/* Brand Section */}
          <div className="lg:col-span-1">
            <Link href="/" className="inline-flex items-center gap-3 group mb-4">
              {settings?.logoUrl ? (
                <div className="h-11 w-11 rounded-xl overflow-hidden shadow-lg group-hover:scale-105 transition-transform">
                  <img
                    src={settings.logoUrl}
                    alt={settings.clinicName || common('clinicName')}
                    className="w-full h-full object-cover"
                  />
                </div>
              ) : (
                <div className="h-11 w-11 rounded-xl bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center shadow-lg group-hover:scale-105 transition-transform">
                  <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
              )}
              <div className="flex flex-col">
                <span className="font-bold text-lg text-slate-900 dark:text-slate-100 leading-tight">
                  {loading ? common('loading') : settings?.clinicName || common('clinicName')}
                </span>
                <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">{settings?.tagline || common('tagline')}</span>
              </div>
            </Link>
            <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed mb-4">
              {t('description')}
            </p>
            {/* Social Links */}
            <div className="flex items-center gap-3">
              {settings?.socialMedia?.facebook && (
                <a
                  href={settings.socialMedia.facebook}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-10 h-10 rounded-lg bg-white dark:bg-slate-800 hover:bg-gradient-to-br hover:from-blue-600 hover:to-cyan-600 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-400 hover:text-white transition-all shadow-sm hover:shadow-md group"
                  aria-label="Facebook"
                >
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                  </svg>
                </a>
              )}
              {settings?.socialMedia?.instagram && (
                <a
                  href={settings.socialMedia.instagram}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-10 h-10 rounded-lg bg-white dark:bg-slate-800 hover:bg-gradient-to-br hover:from-blue-600 hover:to-cyan-600 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-400 hover:text-white transition-all shadow-sm hover:shadow-md group"
                  aria-label="Instagram"
                >
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/>
                  </svg>
                </a>
              )}
              {settings?.socialMedia?.twitter && (
                <a
                  href={settings.socialMedia.twitter}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-10 h-10 rounded-lg bg-white dark:bg-slate-800 hover:bg-gradient-to-br hover:from-blue-600 hover:to-cyan-600 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-400 hover:text-white transition-all shadow-sm hover:shadow-md group"
                  aria-label="Twitter"
                >
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z"/>
                  </svg>
                </a>
              )}
              {settings?.socialMedia?.linkedin && (
                <a
                  href={settings.socialMedia.linkedin}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-10 h-10 rounded-lg bg-white dark:bg-slate-800 hover:bg-gradient-to-br hover:from-blue-600 hover:to-cyan-600 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-400 hover:text-white transition-all shadow-sm hover:shadow-md group"
                  aria-label="LinkedIn"
                >
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
                  </svg>
                </a>
              )}
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="font-bold text-slate-900 dark:text-slate-100 mb-4 text-sm uppercase tracking-wider">{t('quickLinks')}</h3>
            <ul className="space-y-3">
              <li>
                <Link
                  href="/"
                  className="text-sm text-slate-600 dark:text-slate-400 hover:text-blue-700 dark:hover:text-blue-400 transition-colors inline-flex items-center gap-2 group"
                >
                  <svg className="w-4 h-4 text-blue-600 opacity-0 group-hover:opacity-100 -ml-6 group-hover:ml-0 transition-all" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                  <span>{nav('home')}</span>
                </Link>
              </li>
              <li>
                <Link
                  href="/services"
                  className="text-sm text-slate-600 dark:text-slate-400 hover:text-blue-700 dark:hover:text-blue-400 transition-colors inline-flex items-center gap-2 group"
                >
                  <svg className="w-4 h-4 text-blue-600 opacity-0 group-hover:opacity-100 -ml-6 group-hover:ml-0 transition-all" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                  <span>{nav('services')}</span>
                </Link>
              </li>
              <li>
                <Link
                  href="/doctors"
                  className="text-sm text-slate-600 dark:text-slate-400 hover:text-blue-700 dark:hover:text-blue-400 transition-colors inline-flex items-center gap-2 group"
                >
                  <svg className="w-4 h-4 text-blue-600 opacity-0 group-hover:opacity-100 -ml-6 group-hover:ml-0 transition-all" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                  <span>{nav('doctors')}</span>
                </Link>
              </li>
              <li>
                <Link
                  href={bookingSectionPath}
                  locale={false}
                  className="text-sm text-slate-600 dark:text-slate-400 hover:text-blue-700 dark:hover:text-blue-400 transition-colors inline-flex items-center gap-2 group"
                >
                  <svg className="w-4 h-4 text-blue-600 opacity-0 group-hover:opacity-100 -ml-6 group-hover:ml-0 transition-all" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                  <span>{nav('bookAppointment')}</span>
                </Link>
              </li>
            </ul>
          </div>

          {/* Contact Info */}
          <div>
            <h3 className="font-bold text-slate-900 dark:text-slate-100 mb-4 text-sm uppercase tracking-wider">{t('contactUs')}</h3>
            <ul className="space-y-3">
              {settings?.phone && (
                <li className="flex items-start gap-3">
                  <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center flex-shrink-0 shadow-sm">
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">{t('labels.phone')}</p>
                    <a href={`tel:${settings.phone}`} className="text-sm text-slate-700 dark:text-slate-300 hover:text-blue-700 dark:hover:text-blue-400 font-medium transition-colors">
                      {settings.phone}
                    </a>
                  </div>
                </li>
              )}
              {settings?.email && (
                <li className="flex items-start gap-3">
                  <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center flex-shrink-0 shadow-sm">
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">{t('labels.email')}</p>
                    <a href={`mailto:${settings.email}`} className="text-sm text-slate-700 dark:text-slate-300 hover:text-blue-700 dark:hover:text-blue-400 font-medium transition-colors">
                      {settings.email}
                    </a>
                  </div>
                </li>
              )}
              {(settings?.address || settings?.city || settings?.state || settings?.country) && (
                <li className="flex items-start gap-3">
                  <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center flex-shrink-0 shadow-sm">
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">{t('labels.location')}</p>
                    <p className="text-sm text-slate-700 dark:text-slate-300 font-medium">
                      {[settings.address, settings.city, settings.state, settings.country].filter(Boolean).join(', ')}
                    </p>
                  </div>
                </li>
              )}
            </ul>
          </div>

          {/* Hours */}
          <div>
            <h3 className="font-bold text-slate-900 dark:text-slate-100 mb-4 text-sm uppercase tracking-wider">{t('officeHours')}</h3>
            <div className="space-y-2">
              {loading ? (
                <p className="text-sm text-slate-600 dark:text-slate-400">{common('loadingHours')}</p>
              ) : settings?.workingHours ? (
                <>
                  {settings.workingHours.monday && (
                    <div className="flex items-center justify-between p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 shadow-sm">
                      <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">Monday</span>
                      <span className="text-xs text-slate-600 dark:text-slate-400">{settings.workingHours.monday}</span>
                    </div>
                  )}
                  {settings.workingHours.tuesday && (
                    <div className="flex items-center justify-between p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 shadow-sm">
                      <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">Tuesday</span>
                      <span className="text-xs text-slate-600 dark:text-slate-400">{settings.workingHours.tuesday}</span>
                    </div>
                  )}
                  {settings.workingHours.wednesday && (
                    <div className="flex items-center justify-between p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 shadow-sm">
                      <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">Wednesday</span>
                      <span className="text-xs text-slate-600 dark:text-slate-400">{settings.workingHours.wednesday}</span>
                    </div>
                  )}
                  {settings.workingHours.thursday && (
                    <div className="flex items-center justify-between p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 shadow-sm">
                      <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">Thursday</span>
                      <span className="text-xs text-slate-600 dark:text-slate-400">{settings.workingHours.thursday}</span>
                    </div>
                  )}
                  {settings.workingHours.friday && (
                    <div className="flex items-center justify-between p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 shadow-sm">
                      <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">Friday</span>
                      <span className="text-xs text-slate-600 dark:text-slate-400">{settings.workingHours.friday}</span>
                    </div>
                  )}
                  {settings.workingHours.saturday && (
                    <div className="flex items-center justify-between p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 shadow-sm">
                      <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">Saturday</span>
                      <span className={`text-xs ${settings.workingHours.saturday.toLowerCase().includes('closed') ? 'text-red-600 dark:text-red-400 font-medium' : 'text-slate-600 dark:text-slate-400'}`}>
                        {settings.workingHours.saturday}
                      </span>
                    </div>
                  )}
                  {settings.workingHours.sunday && (
                    <div className="flex items-center justify-between p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 shadow-sm">
                      <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">Sunday</span>
                      <span className={`text-xs ${settings.workingHours.sunday.toLowerCase().includes('closed') ? 'text-red-600 dark:text-red-400 font-medium' : 'text-slate-600 dark:text-slate-400'}`}>
                        {settings.workingHours.sunday}
                      </span>
                    </div>
                  )}
                </>
              ) : (
                <div className="text-sm text-slate-500 dark:text-slate-400">{t('noHours')}</div>
              )}
            </div>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="mt-12 pt-8 border-t border-slate-200 dark:border-slate-800">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4">
            <p className="text-sm text-slate-600 dark:text-slate-400">
              {t('copyright', { year: currentYear, clinic: settings?.clinicName || common('clinicName') })}
            </p>
            <div className="flex items-center gap-6">
              <Link href="/privacy" className="text-sm text-slate-600 dark:text-slate-400 hover:text-blue-700 dark:hover:text-blue-400 transition-colors font-medium">
                {t('links.privacy')}
              </Link>
              <Link href="/terms" className="text-sm text-slate-600 dark:text-slate-400 hover:text-blue-700 dark:hover:text-blue-400 transition-colors font-medium">
                {t('links.terms')}
              </Link>
              <Link href="/cookies" className="text-sm text-slate-600 dark:text-slate-400 hover:text-blue-700 dark:hover:text-blue-400 transition-colors font-medium">
                {t('links.cookies')}
              </Link>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
