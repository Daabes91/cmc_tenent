'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useLocale, useTranslations } from 'next-intl';
import { useAuth } from '@/hooks/useAuth';
import { api } from '@/lib/api';
import type { PatientAppointment, TreatmentPlan } from '@/lib/types';
import { ProfileImageUpload } from '@/components/ProfileImageUpload';
import { getBookingSectionPath } from '@/utils/basePath';

export default function DashboardPage() {
  const router = useRouter();
  const locale = useLocale();
  const t = useTranslations('patientDashboard');
  const { user, isAuthenticated, loading, updateUser } = useAuth();
  const [appointments, setAppointments] = useState<PatientAppointment[]>([]);
  const [appointmentsLoading, setAppointmentsLoading] = useState(true);
  const [appointmentsError, setAppointmentsError] = useState<string | null>(null);
  const [appointmentsPage, setAppointmentsPage] = useState(1);
  const appointmentsPerPage = 3;
  const bookingSectionPath = getBookingSectionPath(locale);

  const parseDateInput = (value: unknown): Date | null => {
    if (!value) return null;
    if (value instanceof Date) {
      return Number.isNaN(value.getTime()) ? null : value;
    }
    if (Array.isArray(value) && value.length >= 3) {
      const [year, month, day] = value;
      const date = new Date(Number(year), Number(month) - 1, Number(day));
      return Number.isNaN(date.getTime()) ? null : date;
    }
    if (typeof value === 'object') {
      const record = value as Record<string, unknown>;
      if (Array.isArray(record.value)) {
        return parseDateInput(record.value);
      }
      const year = record.year;
      const month = record.monthValue ?? record.month;
      const day = record.dayOfMonth ?? record.day;
      if (typeof year === 'number' && typeof day === 'number') {
        const monthIndex = typeof month === 'number' ? month - 1 : 0;
        const date = new Date(year, monthIndex, day);
        return Number.isNaN(date.getTime()) ? null : date;
      }
    }
    if (typeof value === 'string') {
      const trimmed = value.trim();
      if (!trimmed) return null;
      const parsed = Date.parse(trimmed);
      if (!Number.isNaN(parsed)) {
        return new Date(parsed);
      }
    }
    if (typeof value === 'number' && Number.isFinite(value)) {
      return new Date(value < 1e12 ? value * 1000 : value);
    }
    return null;
  };

  const formatDateValue = (value: unknown, options: Intl.DateTimeFormatOptions) => {
    const date = parseDateInput(value);
    if (!date) return '—';
    try {
      return date.toLocaleDateString(locale, options);
    } catch {
      return date.toISOString().slice(0, 10);
    }
  };

  const toInputDateValue = (value: unknown) => {
    const date = parseDateInput(value);
    if (!date) return '';
    return date.toISOString().slice(0, 10);
  };

  const formatTime = (value: string) => {
    try {
      return new Date(value).toLocaleTimeString(locale, {
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return value;
    }
  };

  const translateAppointmentStatus = (status: PatientAppointment['status']) =>
    t(`status.appointments.${status}` as any);

  const translatePlanStatus = (status: TreatmentPlan['status']) =>
    t(`status.plans.${status}` as any);

  const translateBookingMode = (mode: PatientAppointment['bookingMode']) =>
    t(`modes.${mode}` as any);

  // Treatment Plans state
  const [treatmentPlans, setTreatmentPlans] = useState<TreatmentPlan[]>([]);
  const [treatmentPlansLoading, setTreatmentPlansLoading] = useState(true);
  const [treatmentPlansError, setTreatmentPlansError] = useState<string | null>(null);

  // Profile edit state
  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [profileData, setProfileData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    profileImageUrl: '',
    dateOfBirth: '',
  });
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);
  const fourteenYearsAgo = new Date();
  fourteenYearsAgo.setFullYear(fourteenYearsAgo.getFullYear() - 14);
  const maxBirthDate = fourteenYearsAgo.toISOString().slice(0, 10);
  const minBirthDate = '1900-01-01';

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.push('/login');
    }
  }, [loading, isAuthenticated, router]);

  useEffect(() => {
    const fetchAppointments = async () => {
      if (!isAuthenticated) return;

      try {
        setAppointmentsLoading(true);
        setAppointmentsError(null);
        const data = await api.getMyAppointments();
        setAppointments(data as PatientAppointment[]);
      } catch (error) {
        console.error('Failed to fetch appointments:', error);
        setAppointmentsError(t('errors.appointments'));
      } finally {
        setAppointmentsLoading(false);
      }
    };

    fetchAppointments();
  }, [isAuthenticated, t]);

  useEffect(() => {
    const fetchTreatmentPlans = async () => {
      if (!isAuthenticated) return;

      try {
        setTreatmentPlansLoading(true);
        setTreatmentPlansError(null);
        const data = await api.getMyTreatmentPlans();
        setTreatmentPlans(data);
      } catch (error) {
        console.error('Failed to fetch treatment plans:', error);
        setTreatmentPlansError(t('errors.treatmentPlans'));
      } finally {
        setTreatmentPlansLoading(false);
      }
    };

    fetchTreatmentPlans();
  }, [isAuthenticated, t]);

  const handleEditProfile = async () => {
    if (!user) return;

    try {
      const profile = await api.getMyProfile();
      setProfileData({
        firstName: profile.firstName,
        lastName: profile.lastName,
        email: profile.email,
        phone: profile.phone,
        profileImageUrl: profile.profileImageUrl || '',
        dateOfBirth: toInputDateValue(profile.dateOfBirth ?? null),
      });
      setIsEditingProfile(true);
      setProfileError(null);
    } catch (error) {
      console.error('Failed to load profile:', error);
      setProfileError(t('errors.profileLoad'));
    }
  };

  const handleImageUploaded = async (imageUrl: string) => {
    if (!user) return;

    try {
      setProfileLoading(true);
      setProfileError(null);

      // Get current profile data
      const profile = await api.getMyProfile();

      // Update profile with new image URL
      const updated = await api.updateMyProfile({
        firstName: profile.firstName,
        lastName: profile.lastName,
        email: profile.email,
        phone: profile.phone,
        profileImageUrl: imageUrl,
        dateOfBirth: profile.dateOfBirth || null,
      });

      updateUser({
        ...user,
        profileImageUrl: imageUrl,
      });

      setProfileData({
        ...profileData,
        profileImageUrl: imageUrl,
        dateOfBirth: toInputDateValue(profile.dateOfBirth ?? null),
      });
    } catch (error) {
      console.error('Failed to update profile image:', error);
      setProfileError(t('errors.profileImage'));
    } finally {
      setProfileLoading(false);
    }
  };

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileLoading(true);
    setProfileError(null);

    try {
      if (profileData.dateOfBirth) {
        const dob = new Date(profileData.dateOfBirth);
        if (Number.isNaN(dob.getTime()) || dob > fourteenYearsAgo) {
          setProfileError(t('errors.invalidDob'));
          setProfileLoading(false);
          return;
        }
      }

      const updated = await api.updateMyProfile({
        ...profileData,
        dateOfBirth: profileData.dateOfBirth ? profileData.dateOfBirth : null,
      });
      updateUser(updated);
      setIsEditingProfile(false);
    } catch (error) {
      console.error('Failed to update profile:', error);
      setProfileError(t('errors.profileUpdate'));
    } finally {
      setProfileLoading(false);
    }
  };

  const handleCancelEdit = () => {
    setIsEditingProfile(false);
    setProfileError(null);
  };

  if (loading) {
    return (
      <main className="min-h-screen bg-slate-50 dark:bg-slate-950">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-20">
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="inline-block animate-spin rounded-full h-16 w-16 border-4 border-blue-600 border-t-transparent"></div>
              <p className="mt-4 text-slate-600 dark:text-slate-300 font-medium">{t('loading')}</p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (!isAuthenticated || !user) {
    return null;
  }

  const firstName = user.name?.split(' ')[0] ?? t('profile.userFallback');

  // Calculate stats
  const upcomingAppointments = appointments.filter(
    (a) => a.status === 'SCHEDULED' || a.status === 'CONFIRMED'
  ).length;
  const activeTreatments = treatmentPlans.filter((t) => t.status === 'IN_PROGRESS').length;
  const completedVisits = treatmentPlans.reduce((sum, t) => sum + t.completedVisits, 0);
  const totalAppointmentPages =
    upcomingAppointments > 0 ? Math.ceil(upcomingAppointments / appointmentsPerPage) : 1;

  return (
    <main className="min-h-screen bg-slate-50 dark:bg-slate-950">
      {/* Modern Header */}
      <section className="bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100">
                {t('header.welcome', { name: firstName })}
              </h1>
              <p className="text-slate-600 dark:text-slate-300 mt-1">{t('header.subtitle')}</p>
            </div>
            <Link
              href={bookingSectionPath}
              locale={false}
              className="hidden md:inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-semibold rounded-xl hover:shadow-lg transition-all"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              {t('header.newAppointment')}
            </Link>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-8">
            <div className="bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-950/30 dark:to-blue-900/30 rounded-xl p-6 border border-blue-200 dark:border-blue-900/50">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-blue-600 dark:text-blue-400 mb-1">
                    {t('stats.upcoming.title')}
                  </p>
                  <p className="text-3xl font-bold text-blue-900 dark:text-blue-100">{upcomingAppointments}</p>
                  <p className="text-xs text-blue-600 dark:text-blue-400 mt-1">{t('stats.upcoming.caption')}</p>
                </div>
                <div className="w-12 h-12 rounded-lg bg-blue-600 dark:bg-blue-500 flex items-center justify-center">
                  <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>
            </div>

            <div className="bg-gradient-to-br from-green-50 to-green-100 dark:from-green-950/30 dark:to-green-900/30 rounded-xl p-6 border border-green-200 dark:border-green-900/50">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-green-600 dark:text-green-400 mb-1">
                    {t('stats.active.title')}
                  </p>
                  <p className="text-3xl font-bold text-green-900 dark:text-green-100">{activeTreatments}</p>
                  <p className="text-xs text-green-600 dark:text-green-400 mt-1">{t('stats.active.caption')}</p>
                </div>
                <div className="w-12 h-12 rounded-lg bg-green-600 dark:bg-green-500 flex items-center justify-center">
                  <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                  </svg>
                </div>
              </div>
            </div>

            <div className="bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-950/30 dark:to-purple-900/30 rounded-xl p-6 border border-purple-200 dark:border-purple-900/50">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-purple-600 dark:text-purple-400 mb-1">
                    {t('stats.completed.title')}
                  </p>
                  <p className="text-3xl font-bold text-purple-900 dark:text-purple-100">{completedVisits}</p>
                  <p className="text-xs text-purple-600 dark:text-purple-400 mt-1">{t('stats.completed.caption')}</p>
                </div>
                <div className="w-12 h-12 rounded-lg bg-purple-600 dark:bg-purple-500 flex items-center justify-center">
                  <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Main Content */}
      <section className="py-8">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="grid lg:grid-cols-3 gap-8">
            {/* Main Column */}
            <div className="lg:col-span-2 space-y-6">
              {/* Upcoming Appointments */}
              <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
                <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
                  <h2 className="text-lg font-bold text-slate-900 dark:text-slate-100">{t('appointments.title')}</h2>
                  <Link href="/dashboard/appointments" className="text-sm font-semibold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300">
                    {t('appointments.viewAll')}
                  </Link>
                </div>

                <div className="p-6">
                  {appointmentsLoading ? (
                    <div className="text-center py-12">
                      <div className="inline-block animate-spin rounded-full h-10 w-10 border-4 border-blue-600 border-t-transparent"></div>
                    </div>
                  ) : appointmentsError ? (
                    <div className="text-center py-8">
                      <div className="w-16 h-16 rounded-full bg-red-50 dark:bg-red-950/50 flex items-center justify-center mx-auto mb-3">
                        <svg className="w-8 h-8 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                      </div>
                      <p className="text-red-600 dark:text-red-400 font-medium">{appointmentsError}</p>
                    </div>
                  ) : appointments.length === 0 ? (
                    <div className="text-center py-12">
                      <div className="w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center mx-auto mb-4">
                        <svg className="w-8 h-8 text-slate-400 dark:text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                      <p className="text-slate-600 dark:text-slate-300 font-medium mb-4">{t('appointments.emptyTitle')}</p>
                        <Link
                          href={bookingSectionPath}
                          locale={false}
                        className="inline-flex items-center gap-2 px-5 py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 transition-colors"
                      >
                        {t('appointments.emptyCta')}
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                        </svg>
                      </Link>
                    </div>
                  ) : (
                    <>
                      <div className="space-y-3">
                        {appointments
                          .filter((a) => a.status === 'SCHEDULED' || a.status === 'CONFIRMED')
                          .slice((appointmentsPage - 1) * appointmentsPerPage, appointmentsPage * appointmentsPerPage)
                          .map((appointment) => (
                            <div
                              key={appointment.id}
                              className="p-4 rounded-lg border border-slate-200 dark:border-slate-700 hover:border-blue-300 dark:hover:border-blue-600 hover:shadow-sm transition-all"
                            >
                              <div className="flex items-start justify-between mb-2">
                                <div className="flex-1">
                                  <h3 className="font-bold text-slate-900 dark:text-slate-100">{appointment.service}</h3>
                                  <p className="text-sm text-slate-600 dark:text-slate-300 mt-1">
                                    {t('labels.doctor', { name: appointment.doctor.name })}
                                  </p>
                                </div>
                                <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                                  appointment.status === 'CONFIRMED'
                                    ? 'bg-green-100 dark:bg-green-950/50 text-green-700 dark:text-green-400'
                                    : 'bg-blue-100 dark:bg-blue-950/50 text-blue-700 dark:text-blue-400'
                                }`}>
                                  {translateAppointmentStatus(appointment.status)}
                                </span>
                              </div>
                              <div className="flex items-center gap-4 text-sm text-slate-600 dark:text-slate-300">
                                <span className="flex items-center gap-1.5">
                                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                  </svg>
                                  {formatDateValue(appointment.scheduledAt, {
                                    month: 'short',
                                    day: 'numeric',
                                  })}
                                </span>
                                <span className="flex items-center gap-1.5">
                                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                  </svg>
                                  {formatTime(appointment.scheduledAt)}
                                </span>
                                <span className="flex items-center gap-1.5">
                                  {appointment.bookingMode === 'CLINIC_VISIT' ? (
                                    <>
                                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                                      </svg>
                                      {translateBookingMode(appointment.bookingMode)}
                                    </>
                                  ) : (
                                    <>
                                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
                                      </svg>
                                      {translateBookingMode(appointment.bookingMode)}
                                    </>
                                  )}
                                </span>
                              </div>
                            </div>
                          ))}
                      </div>

                      {upcomingAppointments > appointmentsPerPage && (
                        <div className="flex items-center justify-center gap-2 mt-4 pt-4 border-t border-slate-200 dark:border-slate-700">
                          <button
                            onClick={() => setAppointmentsPage(appointmentsPage - 1)}
                            disabled={appointmentsPage === 1}
                            className="px-3 py-1.5 text-sm font-medium text-slate-700 dark:text-slate-300 rounded-lg border border-slate-300 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            {t('pagination.previous')}
                          </button>
                          <span className="text-sm text-slate-600 dark:text-slate-300">
                            {t('pagination.pageInfo', {
                              current: appointmentsPage,
                              total: totalAppointmentPages,
                            })}
                          </span>
                          <button
                            onClick={() => setAppointmentsPage(appointmentsPage + 1)}
                            disabled={appointmentsPage === totalAppointmentPages}
                            className="px-3 py-1.5 text-sm font-medium text-slate-700 dark:text-slate-300 rounded-lg border border-slate-300 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            {t('pagination.next')}
                          </button>
                        </div>
                      )}
                    </>
                  )}
                </div>
              </div>

              {/* Treatment Plans */}
              <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
                <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-700">
                  <h2 className="text-lg font-bold text-slate-900 dark:text-slate-100">{t('treatmentPlans.title')}</h2>
                </div>

                <div className="p-6">
                  {treatmentPlansLoading ? (
                    <div className="text-center py-12">
                      <div className="inline-block animate-spin rounded-full h-10 w-10 border-4 border-blue-600 border-t-transparent"></div>
                    </div>
                  ) : treatmentPlansError ? (
                    <div className="text-center py-8">
                      <p className="text-red-600 dark:text-red-400">{treatmentPlansError}</p>
                    </div>
                  ) : treatmentPlans.filter((t) => t.status === 'IN_PROGRESS' || t.status === 'PLANNING').length ===
                    0 ? (
                    <div className="text-center py-12">
                      <div className="w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center mx-auto mb-4">
                        <svg className="w-8 h-8 text-slate-400 dark:text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                        </svg>
                      </div>
                      <p className="text-slate-600 dark:text-slate-300 font-medium">{t('treatmentPlans.emptyTitle')}</p>
                      <p className="text-slate-500 dark:text-slate-400 text-sm mt-2">{t('treatmentPlans.emptySubtitle')}</p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {treatmentPlans
                        .filter((t) => t.status === 'IN_PROGRESS' || t.status === 'PLANNING')
                        .map((plan) => {
                          const progressPercentage =
                            plan.plannedFollowups > 0
                              ? (plan.completedVisits / plan.plannedFollowups) * 100
                              : 0;

                          return (
                            <div
                              key={plan.id}
                              className="p-5 rounded-lg border border-slate-200 dark:border-slate-700 hover:border-blue-300 dark:hover:border-blue-600 hover:shadow-sm transition-all"
                            >
                              <div className="flex items-start justify-between mb-3">
                                <div>
                                  <h3 className="font-bold text-slate-900 dark:text-slate-100">{plan.treatmentTypeName}</h3>
                                  <p className="text-sm text-slate-600 dark:text-slate-300 mt-1">
                                    {t('labels.doctor', { name: plan.doctorName })}
                                  </p>
                                </div>
                                <span
                                  className={`px-3 py-1 rounded-full text-xs font-bold ${
                                    plan.status === 'IN_PROGRESS'
                                      ? 'bg-blue-100 dark:bg-blue-950/50 text-blue-700 dark:text-blue-400'
                                      : 'bg-yellow-100 dark:bg-yellow-950/50 text-yellow-700 dark:text-yellow-400'
                                  }`}
                                >
                                  {translatePlanStatus(plan.status)}
                                </span>
                              </div>

                              {/* Progress */}
                              <div className="mb-3">
                                <div className="flex items-center justify-between mb-2">
                                  <span className="text-xs font-medium text-slate-600 dark:text-slate-300">
                                    {t('labels.progress')}
                                  </span>
                                  <span className="text-xs font-bold text-slate-900 dark:text-slate-100">
                                    {t('labels.visits', {
                                      completed: plan.completedVisits,
                                      planned: plan.plannedFollowups,
                                    })}
                                  </span>
                                </div>
                                <div className="w-full h-2 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                                  <div
                                    className="h-full bg-gradient-to-r from-blue-600 to-cyan-600 transition-all"
                                    style={{ width: `${progressPercentage}%` }}
                                  />
                                </div>
                              </div>

                              {/* Financial */}
                              <div className="grid grid-cols-3 gap-3">
                                <div className="text-center p-2 bg-slate-50 dark:bg-slate-900 rounded-lg">
                                  <p className="text-xs text-slate-600 dark:text-slate-300 mb-1">{t('labels.total')}</p>
                                  <p className="text-sm font-bold text-slate-900 dark:text-slate-100">
                                    {plan.currency} {plan.totalPrice.toFixed(0)}
                                  </p>
                                </div>
                                <div className="text-center p-2 bg-green-50 dark:bg-green-950/30 rounded-lg">
                                  <p className="text-xs text-green-600 dark:text-green-400 mb-1">{t('labels.paid')}</p>
                                  <p className="text-sm font-bold text-green-700 dark:text-green-400">
                                    {plan.currency} {plan.totalPaid.toFixed(0)}
                                  </p>
                                </div>
                                <div className="text-center p-2 bg-orange-50 dark:bg-orange-950/30 rounded-lg">
                                  <p className="text-xs text-orange-600 dark:text-orange-400 mb-1">{t('labels.balance')}</p>
                                  <p className="text-sm font-bold text-orange-700 dark:text-orange-400">
                                    {plan.currency} {plan.remainingBalance.toFixed(0)}
                                  </p>
                                </div>
                              </div>

                              {/* Next Visit */}
                              {plan.scheduledFollowUps &&
                                plan.scheduledFollowUps.filter((f) => f.status === 'SCHEDULED' || f.status === 'CONFIRMED').length > 0 && (
                                  <div className="mt-3 pt-3 border-t border-slate-200 dark:border-slate-700">
                                    <p className="text-xs font-medium text-slate-600 dark:text-slate-300 mb-2">
                                      {t('labels.nextVisit')}
                                    </p>
                                    {plan.scheduledFollowUps
                                      .filter((f) => f.status === 'SCHEDULED' || f.status === 'CONFIRMED')
                                      .slice(0, 1)
                                      .map((visit) => (
                                        <div
                                          key={visit.appointmentId}
                                          className="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300"
                                        >
                                          <svg className="w-4 h-4 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                          </svg>
                                          {formatDateValue(visit.scheduledAt, {
                                            weekday: 'short',
                                            month: 'short',
                                            day: 'numeric',
                                          })}
                                        </div>
                                      ))}
                                  </div>
                                )}
                            </div>
                          );
                        })}
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Sidebar */}
            <div className="lg:col-span-1 space-y-6">
              {/* Profile Card */}
              <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
                <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
                  <h2 className="text-lg font-bold text-slate-900 dark:text-slate-100">{t('profile.title')}</h2>
                  {!isEditingProfile && (
                    <button
                      onClick={handleEditProfile}
                      className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
                      title={t('profile.editLabel')}
                    >
                      <svg className="w-4 h-4 text-slate-600 dark:text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                        />
                      </svg>
                    </button>
                  )}
                </div>

                <div className="p-6">
                  {isEditingProfile ? (
                    <form onSubmit={handleSaveProfile} className="space-y-4">
                      {profileError && (
                        <div className="p-3 rounded-lg bg-red-50 dark:bg-red-950/50 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 text-sm">
                          {profileError}
                        </div>
                      )}

                      <div className="space-y-3">
                        <div>
                          <label className="block text-xs font-medium text-slate-600 dark:text-slate-300 mb-1">
                            {t('profile.form.firstName')}
                          </label>
                          <input
                            type="text"
                            value={profileData.firstName}
                            onChange={(e) => setProfileData({ ...profileData, firstName: e.target.value })}
                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 dark:focus:ring-blue-800 focus:outline-none text-sm"
                            required
                          />
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-slate-600 dark:text-slate-300 mb-1">
                            {t('profile.form.lastName')}
                          </label>
                          <input
                            type="text"
                            value={profileData.lastName}
                            onChange={(e) => setProfileData({ ...profileData, lastName: e.target.value })}
                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 dark:focus:ring-blue-800 focus:outline-none text-sm"
                            required
                          />
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-slate-600 dark:text-slate-300 mb-1">
                            {t('profile.form.email')}
                          </label>
                          <input
                            type="email"
                            value={profileData.email}
                            onChange={(e) => setProfileData({ ...profileData, email: e.target.value })}
                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 dark:focus:ring-blue-800 focus:outline-none text-sm"
                            required
                          />
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-slate-600 dark:text-slate-300 mb-1">
                            {t('profile.form.phone')}
                          </label>
                          <input
                            type="tel"
                            value={profileData.phone}
                            onChange={(e) => setProfileData({ ...profileData, phone: e.target.value })}
                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 dark:focus:ring-blue-800 focus:outline-none text-sm"
                            required
                          />
                        </div>
                        <div>
                          <div className="flex items-center justify-between mb-1">
                            <label className="block text-xs font-medium text-slate-600 dark:text-slate-300">
                              {t('profile.form.dateOfBirth')}
                            </label>
                            <span className="text-[11px] text-slate-500 dark:text-slate-400">
                              {t('profile.form.dateOfBirthHint')}
                            </span>
                          </div>
                          <input
                            type="date"
                            value={profileData.dateOfBirth}
                            onChange={(e) => setProfileData({ ...profileData, dateOfBirth: e.target.value })}
                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 dark:focus:ring-blue-800 focus:outline-none text-sm"
                            max={maxBirthDate}
                            min={minBirthDate}
                          />
                        </div>
                      </div>

                      <div className="flex gap-2 pt-2">
                        <button
                          type="submit"
                          disabled={profileLoading}
                          className="flex-1 px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                        >
                          {profileLoading ? t('profile.buttons.saving') : t('profile.buttons.save')}
                        </button>
                        <button
                          type="button"
                          onClick={handleCancelEdit}
                          disabled={profileLoading}
                          className="px-4 py-2 border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-300 text-sm font-semibold rounded-lg hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors disabled:opacity-50"
                        >
                          {t('profile.buttons.cancel')}
                        </button>
                      </div>
                    </form>
                  ) : (
                    <div className="space-y-6">
                      {/* Profile Image Upload */}
                      <ProfileImageUpload
                        currentImageUrl={user.profileImageUrl}
                        onImageUploaded={handleImageUploaded}
                        userName={user.name || t('profile.userFallback')}
                      />

                      {/* User Info */}
                      <div className="text-center space-y-1">
                        <p className="font-bold text-slate-900 dark:text-slate-100 text-lg">{user.name}</p>
                        <p className="text-sm text-slate-600 dark:text-slate-300">{user.email}</p>
                        {user.dateOfBirth && (
                          <p className="text-xs text-slate-500 dark:text-slate-400">
                            {t('profile.labels.dateOfBirth', {
                              date: formatDateValue(user.dateOfBirth, { dateStyle: 'medium' }),
                            })}
                          </p>
                        )}
                      </div>

                      {/* Error Message */}
                      {profileError && (
                        <div className="p-3 rounded-lg bg-red-50 dark:bg-red-950/50 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 text-sm text-center">
                          {profileError}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>

              {/* Quick Actions */}
              <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
                <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-700">
                  <h2 className="text-lg font-bold text-slate-900 dark:text-slate-100">{t('quickActions.title')}</h2>
                </div>
                <div className="p-4 space-y-2">
                    <Link
                      href={bookingSectionPath}
                      locale={false}
                    className="flex items-center gap-3 p-3 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors group"
                  >
                    <div className="w-10 h-10 rounded-lg bg-blue-100 dark:bg-blue-950/50 flex items-center justify-center group-hover:bg-blue-200 dark:group-hover:bg-blue-900/50 transition-colors">
                      <svg className="w-5 h-5 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                        />
                      </svg>
                    </div>
                    <div className="flex-1">
                      <p className="font-semibold text-slate-900 dark:text-slate-100 text-sm">
                        {t('quickActions.items.appointments.title')}
                      </p>
                      <p className="text-xs text-slate-600 dark:text-slate-300">
                        {t('quickActions.items.appointments.subtitle')}
                      </p>
                    </div>
                  </Link>

                  <Link
                    href="/services"
                    className="flex items-center gap-3 p-3 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors group"
                  >
                    <div className="w-10 h-10 rounded-lg bg-green-100 dark:bg-green-950/50 flex items-center justify-center group-hover:bg-green-200 dark:group-hover:bg-green-900/50 transition-colors">
                      <svg className="w-5 h-5 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
                        />
                      </svg>
                    </div>
                    <div className="flex-1">
                      <p className="font-semibold text-slate-900 dark:text-slate-100 text-sm">
                        {t('quickActions.items.services.title')}
                      </p>
                      <p className="text-xs text-slate-600 dark:text-slate-300">
                        {t('quickActions.items.services.subtitle')}
                      </p>
                    </div>
                  </Link>

                  <Link
                    href="/doctors"
                    className="flex items-center gap-3 p-3 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors group"
                  >
                    <div className="w-10 h-10 rounded-lg bg-purple-100 dark:bg-purple-950/50 flex items-center justify-center group-hover:bg-purple-200 dark:group-hover:bg-purple-900/50 transition-colors">
                      <svg className="w-5 h-5 text-purple-600 dark:text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                        />
                      </svg>
                    </div>
                    <div className="flex-1">
                      <p className="font-semibold text-slate-900 dark:text-slate-100 text-sm">
                        {t('quickActions.items.doctors.title')}
                      </p>
                      <p className="text-xs text-slate-600 dark:text-slate-300">
                        {t('quickActions.items.doctors.subtitle')}
                      </p>
                    </div>
                  </Link>
                </div>
              </div>

              {/* Contact Card */}
              <div className="bg-gradient-to-br from-blue-600 to-cyan-600 rounded-xl p-6 text-white shadow-lg">
                <h3 className="font-bold mb-4">{t('contact.title')}</h3>
                <div className="space-y-3 text-sm">
                  <div className="flex items-center gap-2">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"
                      />
                    </svg>
                    <span>{t('contact.phone')}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                      />
                    </svg>
                    <span>{t('contact.email')}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                    <span>{t('contact.hours')}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
