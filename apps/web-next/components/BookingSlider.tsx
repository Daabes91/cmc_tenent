'use client';

import {useState, useEffect, useMemo} from 'react';
import {useLocale, useTranslations} from 'next-intl';
import {api} from '@/lib/api';
import {useAuth} from '@/hooks/useAuth';
import {useRouter} from 'next/navigation';
import {withBasePath} from '@/utils/basePath';
import type {AvailabilitySlot, Doctor, Service} from '@/lib/types';

export default function BookingSlider() {
  const {isAuthenticated, user} = useAuth();
  const router = useRouter();
  const t = useTranslations('booking');
  const locale = useLocale();
  const isRTL = locale === 'ar';

  const [services, setServices] = useState<Service[]>([]);
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [availableSlots, setAvailableSlots] = useState<AvailabilitySlot[]>([]);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');
  const [paymentSettings, setPaymentSettings] = useState<{
    virtualConsultationFee: number;
    currency: string;
    paypalClientId: string;
    paypalEnvironment: string;
  } | null>(null);

  const [formData, setFormData] = useState({
    serviceSlug: '',
    doctorId: '',
    date: '',
    slot: '',
    consultationType: 'clinic',
    phoneNumber: '',
    guestEmail: '',
    guestName: '',
    notes: ''
  });

  useEffect(() => {
    const loadData = async () => {
      try {
        const [servicesData, doctorsData, paymentData] = await Promise.all([
          api.getServices(locale),
          api.getDoctors(locale),
          api.getPaymentSettings().catch(() => null), // Don't fail if payment settings aren't configured
        ]);
        setServices(servicesData);
        setDoctors(doctorsData);
        setPaymentSettings(paymentData);
      } catch (err) {
        console.error('Failed to load data:', err);
      }
    };
    loadData();
  }, [locale]);

  useEffect(() => {
    if (formData.serviceSlug && formData.doctorId && formData.date) {
      void loadAvailableSlots();
    } else {
      setAvailableSlots([]);
    }
  }, [formData.serviceSlug, formData.doctorId, formData.date]);

  const loadAvailableSlots = async () => {
    try {
      const slots = await api.getAvailabilitySlots(
        parseInt(formData.doctorId, 10),
        formData.serviceSlug,
        formData.date
      );
      setAvailableSlots(slots);
    } catch (err) {
      console.error('Failed to load slots:', err);
      setAvailableSlots([]);
      setError(t('errors.loadSlots'));
    }
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    // Validate required fields
    if (!formData.serviceSlug || !formData.doctorId || !formData.date || !formData.slot) {
      setError(t('errors.requiredFields'));
      return;
    }

    if (!isAuthenticated && !formData.phoneNumber) {
      setError(t('errors.phoneRequired'));
      return;
    }

    setLoading(true);
    setError('');
    setSuccess(false);

    try {
      // Check if this is a virtual consultation that requires payment
      if (formData.consultationType === 'virtual' && paymentSettings && isAuthenticated) {
        const params = new URLSearchParams({
          serviceSlug: formData.serviceSlug,
          doctorId: formData.doctorId,
          slot: formData.slot,
          date: formData.date
        });
        if (formData.notes) {
          params.set('notes', formData.notes);
        }
        const target = withBasePath(`/${locale}/virtual-checkout?${params.toString()}`);
        router.push(target);
        setLoading(false);
        return;
      }

      // For clinic visits or guest bookings, proceed with immediate booking
      if (isAuthenticated) {
        // Authenticated user booking (clinic visit only reaches here)
        await api.createBooking({
          serviceSlug: formData.serviceSlug,
          doctorId: parseInt(formData.doctorId, 10),
          slot: formData.slot,
          bookingMode: 'CLINIC_VISIT',
          notes: formData.notes
        });
      } else {
        await api.createGuestBooking({
          serviceSlug: formData.serviceSlug,
          doctorId: formData.doctorId ? parseInt(formData.doctorId, 10) : undefined,
          slot: formData.slot,
          consultationType: formData.consultationType,
          phoneNumber: formData.phoneNumber,
          guestEmail: formData.guestEmail,
          guestName: formData.guestName,
          notes: formData.notes
        });
      }

      setSuccess(true);
      resetForm();
    } catch (err) {
      const message = err instanceof Error ? err.message : '';
      setError(message || t('errors.submit'));
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      serviceSlug: '',
      doctorId: '',
      date: '',
      slot: '',
      consultationType: 'clinic',
      phoneNumber: '',
      guestEmail: '',
      guestName: '',
      notes: ''
    });
  };

  const handleChange = (
    event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    setFormData((prev) => ({
      ...prev,
      [event.target.name]: event.target.value
    }));
  };

  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minDate = tomorrow.toISOString().split('T')[0];

  const timeFormatter = useMemo(
    () =>
      new Intl.DateTimeFormat(locale === 'ar' ? 'ar-EG' : 'en-US', {
        hour: 'numeric',
        minute: '2-digit'
      }),
    [locale]
  );

  const radioMarginClass = isRTL ? 'ml-2' : 'mr-2';

  // Success screen
  if (success) {
    return (
      <div className="relative z-10 mx-auto max-w-2xl rounded-2xl border border-slate-100/70 dark:border-slate-800/60 bg-white/95 dark:bg-slate-900/80 p-8 shadow-xl dark:shadow-blue-900/40 backdrop-blur transition-colors">
        <div className="text-center">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-green-100 dark:bg-green-500/20">
            <svg className="h-8 w-8 text-green-600 dark:text-green-300" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clipRule="evenodd"
              />
            </svg>
          </div>
          <h3 className="mb-2 text-xl font-bold text-slate-900 dark:text-slate-100">
            {t('status.confirmedTitle')}
          </h3>
          <p className="mb-6 text-slate-600 dark:text-slate-300">{t('status.confirmedSubtitle')}</p>
          <button
            onClick={() => setSuccess(false)}
            className="rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 px-8 py-3 font-semibold text-white transition-all hover:from-blue-700 hover:to-cyan-700 shadow-lg hover:shadow-xl dark:shadow-blue-900/40 dark:hover:shadow-blue-900/60"
          >
            {t('buttons.bookAnother')}
          </button>
        </div>
      </div>
    );
  }

  const heading = isAuthenticated
    ? t('status.welcomeBack', {name: user?.name ?? t('status.patientFallback')})
    : t('quickBooking');

  const subHeading = isAuthenticated ? t('status.scheduleAppointment') : t('quickBookingDesc');

  return (
    <div className="relative z-10 mx-auto max-w-2xl rounded-2xl border border-slate-100/70 dark:border-slate-800/60 bg-white/95 dark:bg-slate-900/80 p-8 shadow-xl dark:shadow-blue-900/40 backdrop-blur transition-colors">
      <div className="mb-6 relative z-[2147483649]">
        <h3 className="text-xl font-bold text-slate-900 dark:text-slate-100 relative z-[2147483649]">{heading}</h3>
        <p className="mt-2 text-slate-600 dark:text-slate-300 relative z-[2147483649]">{subHeading}</p>
      </div>

      {error && (
        <div className="mb-6 rounded-lg border border-red-200 dark:border-red-800/60 bg-red-50 dark:bg-red-950/40 p-4">
          <div className="flex items-start">
            <svg className="mt-0.5 h-5 w-5 text-red-600 dark:text-red-300" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                clipRule="evenodd"
              />
            </svg>
            <p className={`text-sm text-red-700 dark:text-red-300 ${isRTL ? 'mr-3' : 'ml-3'}`}>{error}</p>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Guest Information (only for non-authenticated users) */}
        {!isAuthenticated && (
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">
                {t('fields.guestName')}
              </label>
              <input
                type="text"
                name="guestName"
                value={formData.guestName}
                onChange={handleChange}
                placeholder={t('fields.guestNamePlaceholder')}
                className="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900/80 px-4 py-2.5 text-sm text-slate-900 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:border-transparent focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
              />
            </div>
            <div>
              <label className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">
                {t('fields.guestEmail')}
              </label>
              <input
                type="email"
                name="guestEmail"
                value={formData.guestEmail}
                onChange={handleChange}
                placeholder={t('fields.guestEmailPlaceholder')}
                className="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900/80 px-4 py-2.5 text-sm text-slate-900 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:border-transparent focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
              />
            </div>
            <div className="md:col-span-2">
              <label className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">
                {t('fields.phoneNumber')}{' '}
                <span className="text-red-500">{t('fields.required')}</span>
              </label>
              <input
                type="tel"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleChange}
                required
                placeholder={t('fields.phoneNumberPlaceholder')}
                className="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900/80 px-4 py-2.5 text-sm text-slate-900 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:border-transparent focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
              />
            </div>
          </div>
        )}

        {/* Service and Doctor Selection */}
        <div className="grid gap-4 md:grid-cols-2">
          <div>
            <label className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">
              {t('fields.selectService')}{' '}
              <span className="text-red-500">{t('fields.required')}</span>
            </label>
            <select
              name="serviceSlug"
              value={formData.serviceSlug}
              onChange={handleChange}
              required
              className="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900/80 px-4 py-2.5 text-sm text-slate-900 dark:text-slate-100 focus:border-transparent focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
            >
              <option value="">{t('fields.selectServicePlaceholder')}</option>
              {services.map((service) => (
                <option key={service.slug} value={service.slug}>
                  {service.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">
              {isAuthenticated ? t('fields.selectDoctor') : t('fields.selectDoctorOptional')}{' '}
              <span className="text-red-500">{t('fields.required')}</span>
            </label>
            <select
              name="doctorId"
              value={formData.doctorId}
              onChange={handleChange}
              required={isAuthenticated}
              className="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900/80 px-4 py-2.5 text-sm text-slate-900 dark:text-slate-100 focus:border-transparent focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
            >
              <option value="">
                {isAuthenticated ? t('fields.chooseDoctorOption') : t('fields.anyDoctorOption')}
              </option>
              {doctors.map((doctor) => (
                <option key={doctor.id} value={doctor.id}>
                  {doctor.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Consultation Type */}
        <div>
          <label className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">
            {t('fields.consultationType')}{' '}
            <span className="text-red-500">{t('fields.required')}</span>
          </label>
          <div className="grid grid-cols-2 gap-3">
            <label
              className={`flex cursor-pointer items-center rounded-lg border-2 p-3 transition-colors ${
                formData.consultationType === 'clinic'
                  ? 'border-blue-600 bg-blue-50 dark:border-blue-400 dark:bg-blue-500/10'
                  : 'border-slate-300 hover:border-blue-400 dark:border-slate-700 dark:hover:border-blue-400/70 bg-white/80 dark:bg-slate-900/40'
              }`}
            >
              <input
                type="radio"
                name="consultationType"
                value="clinic"
                checked={formData.consultationType === 'clinic'}
                onChange={handleChange}
                className={radioMarginClass}
              />
              <div>
                <div className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                  {t('fields.inClinic')}
                </div>
                <div className="text-xs text-green-600 dark:text-green-400 font-medium">
                  {locale === 'ar' ? 'لا يوجد دفع مقدم' : 'No upfront payment'}
                </div>
              </div>
            </label>
            <label
              className={`flex cursor-pointer items-center rounded-lg border-2 p-3 transition-colors ${
                formData.consultationType === 'virtual'
                  ? 'border-blue-600 bg-blue-50 dark:border-blue-400 dark:bg-blue-500/10'
                  : 'border-slate-300 hover:border-blue-400 dark:border-slate-700 dark:hover:border-blue-400/70 bg-white/80 dark:bg-slate-900/40'
              }`}
            >
              <input
                type="radio"
                name="consultationType"
                value="virtual"
                checked={formData.consultationType === 'virtual'}
                onChange={handleChange}
                className={radioMarginClass}
                disabled={!isAuthenticated || !paymentSettings || !paymentSettings.virtualConsultationFee}
              />
              <div>
                <div className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                  {t('fields.virtual')}
                </div>
                {paymentSettings && isAuthenticated && paymentSettings.virtualConsultationFee ? (
                  <div className="text-xs text-blue-600 dark:text-blue-400 font-medium">
                    {paymentSettings.currency} {paymentSettings.virtualConsultationFee.toFixed(2)} - {locale === 'ar' ? 'ادفع عبر الإنترنت' : 'Pay online'}
                  </div>
                ) : (
                  <div className="text-xs text-slate-400">
                    {!isAuthenticated ? 'Login required' : 'Not available'}
                  </div>
                )}
              </div>
            </label>
          </div>
          {formData.consultationType === 'virtual' && !isAuthenticated && (
            <div className="mt-2 p-3 bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800/60 rounded-lg">
              <p className="text-sm text-amber-700 dark:text-amber-300">
                {locale === 'ar' 
                  ? 'الاستشارات الافتراضية تتطلب الدفع وهي متاحة فقط للمستخدمين المسجلين.'
                  : 'Virtual consultations require payment and are only available for registered users.'
                }
                <a href="/login" className={`font-semibold underline ${locale === 'ar' ? 'mr-1' : 'ml-1'}`}>
                  {locale === 'ar' ? 'يرجى تسجيل الدخول' : 'Please log in'}
                </a> 
                {locale === 'ar' ? ' للمتابعة.' : ' to continue.'}
              </p>
            </div>
          )}
        </div>

        {/* Date Selection */}
        <div>
          <label className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">
            {t('fields.preferredDate')}{' '}
            <span className="text-red-500">{t('fields.required')}</span>
          </label>
          <input
            type="date"
            name="date"
            value={formData.date}
            onChange={handleChange}
            min={minDate}
            required
            className="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900/80 px-4 py-2.5 text-sm text-slate-900 dark:text-slate-100 focus:border-transparent focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
          />
        </div>

        {/* Time Slot Selection */}
        {availableSlots.length > 0 && (
          <div>
            <label className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">
              {t('fields.availableSlots')}{' '}
              <span className="text-red-500">{t('fields.required')}</span>
            </label>
            <div className="grid max-h-48 grid-cols-3 gap-2 overflow-y-auto rounded-lg border border-slate-200 dark:border-slate-700/80 bg-white/60 dark:bg-slate-900/40 p-3 md:grid-cols-4 transition-colors">
              {availableSlots.map((slot) => (
                <label
                  key={slot.start}
                  className={`flex cursor-pointer items-center justify-center rounded-lg border-2 p-3 transition-colors ${
                    formData.slot === slot.start
                      ? 'border-blue-600 bg-blue-50 dark:border-blue-400 dark:bg-blue-500/10'
                      : 'border-slate-300 hover:border-blue-400 dark:border-slate-700 dark:hover:border-blue-400/70 bg-white/80 dark:bg-slate-900/40'
                  }`}
                >
                  <input
                    type="radio"
                    name="slot"
                    value={slot.start}
                    checked={formData.slot === slot.start}
                    onChange={handleChange}
                    className="sr-only"
                  />
                  <span className="text-sm font-medium text-slate-700 dark:text-slate-100">
                    {timeFormatter.format(new Date(slot.start))}
                  </span>
                </label>
              ))}
            </div>
          </div>
        )}

        {/* Notes */}
        <div>
          <label className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">
            {t('fields.additionalNotes')}
          </label>
          <textarea
            name="notes"
            value={formData.notes}
            onChange={handleChange}
            rows={3}
            placeholder={t('fields.additionalNotesPlaceholder')}
            className="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900/80 px-4 py-2.5 text-sm text-slate-900 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:border-transparent focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 transition-colors"
          />
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={loading || !formData.slot}
          className="w-full rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 py-3.5 font-semibold text-white transition-all hover:from-blue-700 hover:to-cyan-700 disabled:cursor-not-allowed disabled:opacity-50 shadow-lg hover:shadow-xl dark:shadow-blue-900/40 dark:hover:shadow-blue-900/60 transform hover:-translate-y-0.5"
        >
          {loading
            ? t('buttons.loading')
            : formData.consultationType === 'virtual' && paymentSettings && isAuthenticated && paymentSettings.virtualConsultationFee
              ? (locale === 'ar'
                  ? `ادفع ${paymentSettings.currency} ${paymentSettings.virtualConsultationFee.toFixed(2)} للمتابعة`
                  : `Pay ${paymentSettings.currency} ${paymentSettings.virtualConsultationFee.toFixed(2)} to continue`)
              : t('buttons.confirm')}
        </button>
      </form>
    </div>
  );
}
