'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useLocale, useTranslations } from 'next-intl';
import { api } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import type { PatientAppointment } from '@/lib/types';
import { getBookingSectionPath } from '@/utils/basePath';

type AppointmentStatus = PatientAppointment['status'];
type BookingMode = PatientAppointment['bookingMode'];

const STATUS_STYLES: Record<AppointmentStatus, string> = {
  SCHEDULED: 'bg-blue-100 dark:bg-blue-950/50 text-blue-700 dark:text-blue-400',
  CONFIRMED: 'bg-emerald-100 dark:bg-emerald-950/50 text-emerald-700 dark:text-emerald-400',
  COMPLETED: 'bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-300',
  CANCELLED: 'bg-rose-100 dark:bg-rose-950/50 text-rose-700 dark:text-rose-400',
};

const MODE_ICON_BG: Record<BookingMode, string> = {
  CLINIC_VISIT: 'bg-blue-100 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400',
  VIRTUAL_CONSULTATION: 'bg-purple-100 dark:bg-purple-950/50 text-purple-600 dark:text-purple-400',
};

export default function PatientAppointmentsPage() {
  const router = useRouter();
  const locale = useLocale();
  const t = useTranslations('patientAppointments');
  const dashboardT = useTranslations('patientDashboard');
  const { isAuthenticated, loading: authLoading } = useAuth();

  const [appointments, setAppointments] = useState<PatientAppointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const bookingSectionPath = getBookingSectionPath(locale);

  const dateFormatter = useMemo(() => {
    try {
      return new Intl.DateTimeFormat(locale, { dateStyle: 'medium' });
    } catch {
      return new Intl.DateTimeFormat('en', { dateStyle: 'medium' });
    }
  }, [locale]);

  const timeFormatter = useMemo(() => {
    try {
      return new Intl.DateTimeFormat(locale, { hour: '2-digit', minute: '2-digit' });
    } catch {
      return new Intl.DateTimeFormat('en', { hour: '2-digit', minute: '2-digit' });
    }
  }, [locale]);

  const dateTimeFormatter = useMemo(() => {
    try {
      return new Intl.DateTimeFormat(locale, { dateStyle: 'medium', timeStyle: 'short' });
    } catch {
      return new Intl.DateTimeFormat('en', { dateStyle: 'medium', timeStyle: 'short' });
    }
  }, [locale]);

  const translateStatus = (status: AppointmentStatus) =>
    dashboardT(`status.appointments.${status}` as any);

  const translateMode = (mode: BookingMode) => dashboardT(`modes.${mode}` as any);

  const formatDate = (value: string) => {
    try {
      return dateFormatter.format(new Date(value));
    } catch {
      return value;
    }
  };

  const formatTime = (value: string) => {
    try {
      return timeFormatter.format(new Date(value));
    } catch {
      return value;
    }
  };

  const formatDateTime = (value: string) => {
    try {
      return dateTimeFormatter.format(new Date(value));
    } catch {
      return value;
    }
  };

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    const loadAppointments = async () => {
      setLoading(true);
      setError(null);

      try {
        const data = await api.getMyAppointments();
        const sorted = [...(data as PatientAppointment[])].sort((a, b) => {
          const aTime = new Date(a.scheduledAt).getTime();
          const bTime = new Date(b.scheduledAt).getTime();
          return aTime - bTime;
        });
        setAppointments(sorted);
      } catch (err) {
        console.error('[PatientAppointments] Failed to load appointments:', err);
        setError(t('errors.load'));
      } finally {
        setLoading(false);
      }
    };

    void loadAppointments();
  }, [isAuthenticated, t]);

  if (authLoading || (loading && appointments.length === 0)) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50">
        <div className="mx-auto max-w-6xl px-4 md:px-6 lg:px-8 py-24">
          <div className="flex items-center justify-center">
            <div className="text-center">
              <div className="inline-block h-16 w-16 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
              <p className="mt-4 text-slate-600 font-medium">{t('loading')}</p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50">
      {/* Header */}
      <section className="py-16 bg-gradient-to-r from-blue-600 to-cyan-600">
        <div className="mx-auto max-w-6xl px-4 md:px-6 lg:px-8">
          <div className="text-center text-white">
            <h1 className="text-4xl md:text-5xl font-bold">{t('title')}</h1>
            <p className="mt-4 text-lg text-blue-50">{t('subtitle')}</p>

            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Link
                href="/dashboard"
                className="inline-flex items-center gap-2 rounded-xl border border-white/40 px-5 py-3 text-sm font-semibold transition-colors hover:bg-white/10"
              >
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
                {t('actions.back')}
              </Link>
                <Link
                  href={bookingSectionPath}
                  locale={false}
                className="inline-flex items-center gap-2 rounded-xl bg-white px-5 py-3 text-sm font-semibold text-blue-600 shadow hover:bg-blue-50"
              >
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                {t('actions.book')}
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Content */}
      <section className="py-16">
        <div className="mx-auto max-w-6xl px-4 md:px-6 lg:px-8">
          {error && (
            <div className="mb-8 rounded-2xl border border-red-200 bg-red-50 p-4 text-red-700">
              <div className="flex items-center gap-3">
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span className="font-semibold">{error}</span>
              </div>
            </div>
          )}

          {appointments.length === 0 ? (
            <div className="rounded-2xl border-2 border-dashed border-slate-200 bg-white p-12 text-center">
              <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-slate-100">
                <svg className="h-8 w-8 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <h2 className="mt-6 text-xl font-semibold text-slate-900">{t('empty.title')}</h2>
              <p className="mt-2 text-sm text-slate-600">{t('empty.description')}</p>
            </div>
          ) : (
            <div className="space-y-5">
              {appointments.map((appointment) => {
                const statusStyle = STATUS_STYLES[appointment.status];
                const modeStyle = MODE_ICON_BG[appointment.bookingMode];

                return (
                  <div
                    key={appointment.id}
                    className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:border-blue-200 hover:shadow-md"
                  >
                    <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                      <div>
                        <div className="inline-flex items-center gap-3">
                          <span className="text-sm font-semibold uppercase tracking-wide text-slate-500">
                            {t('labels.service')}
                          </span>
                          <span className={`rounded-full px-3 py-1 text-xs font-semibold ${statusStyle}`}>
                            {translateStatus(appointment.status)}
                          </span>
                        </div>
                        <h3 className="mt-2 text-2xl font-bold text-slate-900">{appointment.service}</h3>
                        <p className="mt-1 text-sm text-slate-600">
                          {t('labels.doctor')}: {dashboardT('labels.doctor', { name: appointment.doctor.name })}
                        </p>
                      </div>

                      <div className="flex items-center gap-3">
                        <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${modeStyle}`}>
                          {appointment.bookingMode === 'CLINIC_VISIT' ? (
                            <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 2h6a2 2 0 012 2v4h1a2 2 0 012 2v10a2 2 0 01-2 2H6a2 2 0 01-2-2V10a2 2 0 012-2h1V4a2 2 0 012-2z" />
                            </svg>
                          ) : (
                            <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
                            </svg>
                          )}
                        </div>
                        <div>
                          <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                            {t('labels.mode')}
                          </p>
                          <p className="text-sm font-semibold text-slate-800">
                            {translateMode(appointment.bookingMode)}
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                      <div>
                        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                          {t('labels.scheduled')}
                        </p>
                        <p className="text-sm font-semibold text-slate-900">
                          {formatDate(appointment.scheduledAt)}
                        </p>
                        <p className="text-xs text-slate-600">{formatTime(appointment.scheduledAt)}</p>
                      </div>

                      <div>
                        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                          {t('labels.booked')}
                        </p>
                        <p className="text-sm font-semibold text-slate-900">
                          {formatDateTime(appointment.createdAt)}
                        </p>
                      </div>

                      <div>
                        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                          {t('labels.doctor')}
                        </p>
                        <p className="text-sm font-semibold text-slate-900">
                          {appointment.doctor.name}
                        </p>
                        <p className="text-xs text-slate-600">{appointment.doctor.specialization}</p>
                      </div>

                      <div>
                        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                          {t('labels.id')}
                        </p>
                        <p className="text-sm font-semibold text-slate-900">#{appointment.id}</p>
                      </div>
                    </div>

                    {appointment.notes && (
                      <div className="mt-5 rounded-xl border border-slate-200 bg-slate-50 p-4">
                        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                          {t('labels.notes')}
                        </p>
                        <p className="mt-1 text-sm text-slate-700">{appointment.notes}</p>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
