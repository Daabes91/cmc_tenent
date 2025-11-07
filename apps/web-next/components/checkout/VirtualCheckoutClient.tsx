'use client';

import { useEffect, useMemo, useState } from 'react';
import { useTranslations } from 'next-intl';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import { withBasePath } from '@/utils/basePath';
import PayPalCheckoutIsolated from '@/components/PayPalCheckoutIsolated';

type VirtualCheckoutClientProps = {
  locale: string;
  searchParams: Record<string, string>;
};

type Summary = {
  serviceSlug: string;
  serviceName: string;
  doctorId: number;
  doctorName: string;
  slotIso: string;
  notes?: string;
};

export function VirtualCheckoutClient({ locale, searchParams }: VirtualCheckoutClientProps) {
  const t = useTranslations('booking');
  const { user, isAuthenticated, loading } = useAuth();
  const router = useRouter();

  const [summary, setSummary] = useState<Summary | null>(null);
  const [paymentSettings, setPaymentSettings] = useState<{
    virtualConsultationFee: number;
    currency: string;
  } | null>(null);
  const [initializing, setInitializing] = useState(true);
  const [status, setStatus] = useState<'idle' | 'success'>('idle');
  const [error, setError] = useState<string>('');
  const [orderId, setOrderId] = useState('');
  const [paymentInitializing, setPaymentInitializing] = useState(true);

  const requiresRedirect = useMemo(() => {
    return !searchParams.serviceSlug || !searchParams.doctorId || !searchParams.slot;
  }, [searchParams]);

  useEffect(() => {
    if (loading) return;
    if (requiresRedirect) {
      router.replace(withBasePath(`/${locale}#booking-section`));
      return;
    }
    if (!isAuthenticated) {
      const params = new URLSearchParams({
        redirect: `/virtual-checkout?${new URLSearchParams(searchParams).toString()}`
      });
      router.replace(withBasePath(`/${locale}/login?${params.toString()}`));
      return;
    }

    const hydrate = async () => {
      try {
        const doctorId = Number(searchParams.doctorId);
        const serviceSlug = searchParams.serviceSlug;
        const slotIso = searchParams.slot;
        const notes = searchParams.notes;

        const [services, doctor, payment] = await Promise.all([
          api.getServices(locale),
          api.getDoctor(doctorId, locale),
          api.getPaymentSettings().catch(() => null)
        ]);

        const service = services.find((svc) => svc.slug === serviceSlug);

        setSummary({
          serviceSlug,
          serviceName: service?.name || serviceSlug,
          doctorId,
          doctorName: doctor?.name || '',
          slotIso,
          notes
        });

        if (payment) {
        setPaymentSettings({
          virtualConsultationFee: payment.virtualConsultationFee,
          currency: payment.currency
        });
        setPaymentInitializing(true);
      } else {
        setError(t('payment.paypal.configurationError'));
      }
      } catch (err) {
        console.error('Failed to prepare virtual checkout', err);
        setError(t('payment.paypal.configurationError'));
      } finally {
        setInitializing(false);
      }
    };

    void hydrate();
  }, [requiresRedirect, loading, isAuthenticated, locale, searchParams, t, router]);

  useEffect(() => {
    if (status === 'success') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }, [status]);

  const formattedSlot = useMemo(() => {
    if (!summary?.slotIso) return '';
    try {
      const date = new Date(summary.slotIso);
      return new Intl.DateTimeFormat(locale === 'ar' ? 'ar-EG' : 'en-US', {
        dateStyle: 'full',
        timeStyle: 'short'
      }).format(date);
    } catch {
      return summary.slotIso;
    }
  }, [summary, locale]);

  if (initializing) {
    return (
      <div className="mx-auto max-w-3xl rounded-2xl border border-slate-200/70 dark:border-slate-800 bg-white/80 dark:bg-slate-900/70 p-8 text-center shadow-lg">
        <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-blue-500 border-t-transparent"></div>
        <p className="mt-4 text-sm text-slate-600 dark:text-slate-300">{t('status.loading')}</p>
      </div>
    );
  }

  if (!summary || !user) {
    return (
      <div className="mx-auto max-w-3xl rounded-2xl border border-red-200/60 bg-red-50 dark:bg-red-900/20 dark:border-red-800/60 p-8 text-center shadow-lg">
        <h2 className="text-2xl font-bold text-red-600 dark:text-red-300 mb-3">{t('payment.paypal.configurationError')}</h2>
        <p className="text-sm text-red-600 dark:text-red-300 mb-6">
          {locale === 'ar' ? 'تعذر تجهيز تفاصيل الحجز.' : 'Unable to prepare booking details.'}
        </p>
        <button
          onClick={() => router.push(withBasePath(`/${locale}#booking-section`))}
          className="rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 px-6 py-3 font-semibold text-white transition-all hover:from-blue-700 hover:to-cyan-700"
        >
          {t('buttons.back')}
        </button>
      </div>
    );
  }

  if (!paymentSettings) {
    return (
      <div className="mx-auto max-w-3xl rounded-2xl border border-red-200/60 bg-red-50 dark:bg-red-900/20 dark:border-red-800/60 p-8 text-center shadow-lg">
        <h2 className="text-2xl font-bold text-red-600 dark:text-red-300 mb-3">
          {t('payment.paypal.configurationError')}
        </h2>
        <p className="text-sm text-red-600 dark:text-red-300 mb-6">
          {error || (locale === 'ar' ? 'يرجى التواصل مع العيادة لاستكمال الدفع.' : 'Please contact the clinic to complete your payment.')}
        </p>
        <button
          onClick={() => router.push(withBasePath(`/${locale}#booking-section`))}
          className="rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 px-6 py-3 font-semibold text-white transition-all hover:from-blue-700 hover:to-cyan-700"
        >
          {t('buttons.back')}
        </button>
      </div>
    );
  }

  if (status === 'success') {
    return (
      <div className="mx-auto max-w-3xl rounded-2xl border border-green-200/70 dark:border-green-900/50 bg-green-50 dark:bg-green-900/30 p-8 shadow-xl">
        <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-full bg-green-600 text-white">
          <svg className="h-8 w-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h2 className="text-3xl font-bold text-green-700 dark:text-green-200 mb-4">
          {t('status.confirmedTitle')}
        </h2>
        <p className="text-slate-700 dark:text-slate-200 mb-6">{t('status.confirmedSubtitle')}</p>
        <div className="mb-6 rounded-xl border border-white/20 bg-white/80 dark:bg-slate-900/50 p-6 shadow-inner">
          <dl className="space-y-3 text-sm text-slate-700 dark:text-slate-300">
            <div className="flex justify-between">
              <dt className="font-medium">{t('fields.service')}</dt>
              <dd>{summary.serviceName}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="font-medium">{t('fields.doctor')}</dt>
              <dd>{summary.doctorName}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="font-medium">{t('fields.selectedSlot')}</dt>
              <dd>{formattedSlot}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="font-medium">{t('fields.amount')}</dt>
              <dd>
                {paymentSettings.currency} {paymentSettings.virtualConsultationFee.toFixed(2)}
              </dd>
            </div>
            <div className="flex justify-between">
              <dt className="font-medium">Order ID</dt>
              <dd className="font-mono text-xs">{orderId}</dd>
            </div>
          </dl>
        </div>
        <div className="flex flex-col gap-3 sm:flex-row sm:justify-center">
          <button
            onClick={() => router.push(withBasePath(`/${locale}/dashboard`))}
            className="w-full sm:w-auto rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 px-6 py-3 font-semibold text-white transition-all hover:from-blue-700 hover:to-cyan-700"
          >
            {t('buttons.viewAppointments')}
          </button>
          <button
            onClick={() => router.push(withBasePath(`/${locale}`))}
            className="w-full sm:w-auto rounded-xl border border-slate-200 dark:border-slate-700 bg-white/80 dark:bg-slate-900/50 px-6 py-3 font-semibold text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all"
          >
            {t('buttons.backToHome')}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6">
      <button
        onClick={() => router.push(withBasePath(`/${locale}#booking-section`))}
        className="flex w-fit items-center gap-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-white/70 dark:bg-slate-900/60 px-4 py-2 text-sm font-medium text-slate-600 dark:text-slate-300 transition-colors hover:bg-slate-100 dark:hover:bg-slate-800"
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        {locale === 'ar' ? 'تعديل الحجز' : 'Modify booking'}
      </button>

      <div className="grid gap-6 lg:grid-cols-[2fr_3fr]">
        <section className="rounded-2xl border border-slate-200 dark:border-slate-700 bg-white/90 dark:bg-slate-900/70 p-6 shadow-lg space-y-4">
          <h2 className="text-xl font-semibold text-slate-900 dark:text-slate-100">
            {locale === 'ar' ? 'مراجعة الحجز' : 'Booking summary'}
          </h2>
          <dl className="space-y-3 text-sm text-slate-600 dark:text-slate-300">
            <div>
              <dt className="font-semibold text-slate-700 dark:text-slate-200">{t('fields.service')}</dt>
              <dd>{summary.serviceName}</dd>
            </div>
            <div>
              <dt className="font-semibold text-slate-700 dark:text-slate-200">{t('fields.doctor')}</dt>
              <dd>{summary.doctorName}</dd>
            </div>
            <div>
              <dt className="font-semibold text-slate-700 dark:text-slate-200">{t('fields.selectedSlot')}</dt>
              <dd>{formattedSlot}</dd>
            </div>
            <div>
              <dt className="font-semibold text-slate-700 dark:text-slate-200">{t('fields.amount')}</dt>
              <dd>
                {paymentSettings.currency} {paymentSettings.virtualConsultationFee.toFixed(2)}
              </dd>
            </div>
            {summary.notes && (
              <div>
                <dt className="font-semibold text-slate-700 dark:text-slate-200">{t('fields.additionalNotes')}</dt>
                <dd className="text-sm text-slate-600 dark:text-slate-400 whitespace-pre-wrap">
                  {summary.notes}
                </dd>
              </div>
            )}
          </dl>
        </section>

        <section className="relative rounded-2xl border border-slate-200 dark:border-slate-700 bg-white/95 dark:bg-slate-900/80 p-6 shadow-xl">
          <h2 className="text-xl font-semibold text-slate-900 dark:text-slate-100 mb-4">
            {locale === 'ar' ? 'إتمام الدفع' : 'Complete your payment'}
          </h2>
          <PayPalCheckoutIsolated
            amount={paymentSettings.virtualConsultationFee}
            currency={paymentSettings.currency}
            patientId={user.id}
            doctorId={summary.doctorId}
            serviceId={1}
            slotId={summary.slotIso}
            onSuccess={(id) => {
              setOrderId(id);
              setStatus('success');
            }}
            onError={(message) => setError(message)}
            onCancel={() => {
              setError(locale === 'ar' ? 'تم إلغاء عملية الدفع' : 'Payment was cancelled');
            }}
            onReady={() => setPaymentInitializing(false)}
          />
          {error && status !== 'success' && (
            <p className="mt-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600 dark:bg-red-900/20 dark:border-red-800 dark:text-red-300">
              {error}
            </p>
          )}
          {paymentInitializing && (
            <div className="absolute inset-0 flex items-center justify-center rounded-2xl bg-white/70 dark:bg-slate-900/70 backdrop-blur-sm">
              <div className="flex flex-col items-center gap-3">
                <div className="h-10 w-10 animate-spin rounded-full border-4 border-blue-500 border-t-transparent"></div>
                <p className="text-sm text-slate-600 dark:text-slate-300">
                  {locale === 'ar' ? 'جارٍ تجهيز بوابة الدفع...' : 'Preparing payment gateway...'}
                </p>
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
