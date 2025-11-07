'use client';

import { useState, useEffect } from 'react';
import { useLocale } from 'next-intl';
import { api } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import PayPalCheckoutIsolated from './PayPalCheckoutIsolated';
import type { Service, Doctor, AvailabilitySlot } from '@/lib/types';

export default function BookingForm() {
  const locale = useLocale();
  const { isAuthenticated, user } = useAuth();
  const [services, setServices] = useState<Service[]>([]);
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [availableSlots, setAvailableSlots] = useState<AvailabilitySlot[]>([]);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');
  const [showPayment, setShowPayment] = useState(false);
  const [paymentSettings, setPaymentSettings] = useState<{
    virtualConsultationFee: number;
    currency: string;
    paypalClientId: string;
    paypalEnvironment: string;
  } | null>(null);

  // Form state
  const [formData, setFormData] = useState({
    serviceSlug: '',
    doctorId: '',
    date: '',
    slot: '',
    consultationType: 'clinic',
    phoneNumber: '',
    guestEmail: '',
    guestName: '',
    notes: '',
  });

  // Load services, doctors, and payment settings on mount
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

  // Load available slots when service, doctor, and date are selected
  useEffect(() => {
    if (formData.serviceSlug && formData.doctorId && formData.date) {
      loadAvailableSlots();
    } else {
      setAvailableSlots([]);
    }
  }, [formData.serviceSlug, formData.doctorId, formData.date]);

  const loadAvailableSlots = async () => {
    try {
      const slots = await api.getAvailabilitySlots(
        parseInt(formData.doctorId),
        formData.serviceSlug,
        formData.date
      );
      setAvailableSlots(slots);
    } catch (err) {
      console.error('Failed to load slots:', err);
      setAvailableSlots([]);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess(false);

    try {
      // Check if this is a virtual consultation that requires payment
      if (formData.consultationType === 'virtual' && paymentSettings && isAuthenticated) {
        // Show PayPal checkout for virtual consultations
        setShowPayment(true);
        setLoading(false);
        return;
      }

      // For clinic visits or guest bookings, proceed with immediate booking
      if (isAuthenticated) {
        // Authenticated user booking (clinic visit only reaches here)
        await api.createBooking({
          serviceSlug: formData.serviceSlug,
          doctorId: parseInt(formData.doctorId),
          slot: formData.slot,
          bookingMode: 'CLINIC_VISIT',
          notes: formData.notes,
        });
      } else {
        // Guest booking
        if (!formData.phoneNumber) {
          throw new Error('Phone number is required for guest bookings');
        }
        await api.createGuestBooking({
          serviceSlug: formData.serviceSlug,
          doctorId: formData.doctorId ? parseInt(formData.doctorId) : undefined,
          slot: formData.slot,
          consultationType: formData.consultationType,
          phoneNumber: formData.phoneNumber,
          guestEmail: formData.guestEmail,
          guestName: formData.guestName,
          notes: formData.notes,
        });
      }

      setSuccess(true);
      resetForm();
    } catch (err: any) {
      setError(err.message || 'Failed to create booking');
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
      notes: '',
    });
    setShowPayment(false);
  };

  const handlePaymentSuccess = (orderID: string) => {
    setSuccess(true);
    setShowPayment(false);
    resetForm();
  };

  const handlePaymentError = (error: string) => {
    setError(error);
    setShowPayment(false);
    setLoading(false);
  };

  const handlePaymentCancel = () => {
    setShowPayment(false);
    setLoading(false);
  };

  const handleChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >
  ) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  // Get tomorrow's date as minimum
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minDate = tomorrow.toISOString().split('T')[0];

  return (
    <div className="bg-white rounded-2xl shadow-xl p-8 max-w-2xl mx-auto">
      <div className="mb-6">
        <h3 className="text-2xl font-bold text-slate-900 mb-2">
          {isAuthenticated ? 'Book Your Appointment' : 'Quick Booking'}
        </h3>
        <p className="text-slate-600">
          {isAuthenticated
            ? `Welcome back, ${user?.name || 'Patient'}! Schedule your next visit.`
            : 'Book an appointment without creating an account'}
        </p>
      </div>

      {success && (
        <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg">
          <div className="flex items-start">
            <svg
              className="w-5 h-5 text-green-600 mt-0.5 mr-3"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clipRule="evenodd"
              />
            </svg>
            <div>
              <h4 className="font-semibold text-green-900">Booking Confirmed!</h4>
              <p className="text-green-700 text-sm mt-1">
                Your appointment has been successfully scheduled. We'll send you a
                confirmation shortly.
              </p>
            </div>
          </div>
        </div>
      )}

      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
          <div className="flex items-start">
            <svg
              className="w-5 h-5 text-red-600 mt-0.5 mr-3"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                clipRule="evenodd"
              />
            </svg>
            <p className="text-red-700 text-sm">{error}</p>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Guest Information (only for non-authenticated users) */}
        {!isAuthenticated && (
          <div className="grid md:grid-cols-2 gap-6 pb-6 border-b border-slate-200">
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">
                Your Name (Optional)
              </label>
              <input
                type="text"
                name="guestName"
                value={formData.guestName}
                onChange={handleChange}
                placeholder="John Doe"
                className="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">
                Email (Optional)
              </label>
              <input
                type="email"
                name="guestEmail"
                value={formData.guestEmail}
                onChange={handleChange}
                placeholder="john@example.com"
                className="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">
                Phone Number <span className="text-red-500">*</span>
              </label>
              <input
                type="tel"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleChange}
                required={!isAuthenticated}
                placeholder="+1234567890"
                className="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>
        )}

        {/* Service Selection */}
        <div>
          <label className="block text-sm font-semibold text-slate-700 mb-2">
            Select Service <span className="text-red-500">*</span>
          </label>
          <select
            name="serviceSlug"
            value={formData.serviceSlug}
            onChange={handleChange}
            required
            className="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">Choose a service...</option>
            {services.map((service) => (
              <option key={service.slug} value={service.slug}>
                {service.name}
              </option>
            ))}
          </select>
        </div>

        {/* Doctor Selection */}
        <div>
          <label className="block text-sm font-semibold text-slate-700 mb-2">
            Select Doctor {!isAuthenticated && '(Optional)'}
          </label>
          <select
            name="doctorId"
            value={formData.doctorId}
            onChange={handleChange}
            required={isAuthenticated}
            className="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">
              {isAuthenticated ? 'Choose a doctor...' : 'Any available doctor'}
            </option>
            {doctors.map((doctor) => (
              <option key={doctor.id} value={doctor.id}>
                {doctor.name} - {doctor.specialty ?? 'Specialty not set'}
              </option>
            ))}
          </select>
        </div>

        {/* Consultation Type */}
        <div>
          <label className="block text-sm font-semibold text-slate-700 mb-2">
            Consultation Type <span className="text-red-500">*</span>
          </label>
          <div className="grid grid-cols-2 gap-4">
            <label className="relative flex items-center p-4 border-2 border-slate-300 rounded-lg cursor-pointer hover:border-blue-500 transition-colors">
              <input
                type="radio"
                name="consultationType"
                value="clinic"
                checked={formData.consultationType === 'clinic'}
                onChange={handleChange}
                className="mr-3"
              />
              <div>
                <div className="font-semibold text-slate-900">In-Clinic</div>
                <div className="text-sm text-slate-600">Visit our clinic</div>
                <div className="text-xs text-green-600 font-medium">
                  {locale === 'ar' ? 'لا يوجد دفع مقدم' : 'No upfront payment'}
                </div>
              </div>
            </label>
            <label className="relative flex items-center p-4 border-2 border-slate-300 rounded-lg cursor-pointer hover:border-blue-500 transition-colors">
              <input
                type="radio"
                name="consultationType"
                value="virtual"
                checked={formData.consultationType === 'virtual'}
                onChange={handleChange}
                className="mr-3"
                disabled={!isAuthenticated || !paymentSettings || !paymentSettings.virtualConsultationFee}
              />
              <div>
                <div className="font-semibold text-slate-900">Virtual</div>
                <div className="text-sm text-slate-600">Online consultation</div>
                {paymentSettings && isAuthenticated && paymentSettings.virtualConsultationFee ? (
                  <div className="text-xs text-blue-600 font-medium">
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
            <div className="mt-2 p-3 bg-amber-50 border border-amber-200 rounded-lg">
              <p className="text-sm text-amber-700">
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
          <label className="block text-sm font-semibold text-slate-700 mb-2">
            Preferred Date <span className="text-red-500">*</span>
          </label>
          <input
            type="date"
            name="date"
            value={formData.date}
            onChange={handleChange}
            min={minDate}
            required
            className="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        {/* Time Slot Selection */}
        {availableSlots.length > 0 && (
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-2">
              Available Time Slots <span className="text-red-500">*</span>
            </label>
            <div className="grid grid-cols-3 gap-3">
              {availableSlots.map((slot) => (
                <label
                  key={slot.start}
                  className={`relative flex items-center justify-center p-3 border-2 rounded-lg cursor-pointer transition-colors ${
                    formData.slot === slot.start
                      ? 'border-blue-600 bg-blue-50'
                      : 'border-slate-300 hover:border-blue-400'
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
                  <span className="text-sm font-medium">
                    {new Date(slot.start).toLocaleTimeString('en-US', {
                      hour: 'numeric',
                      minute: '2-digit',
                      hour12: true,
                    })}
                  </span>
                </label>
              ))}
            </div>
          </div>
        )}

        {/* Notes */}
        <div>
          <label className="block text-sm font-semibold text-slate-700 mb-2">
            Additional Notes (Optional)
          </label>
          <textarea
            name="notes"
            value={formData.notes}
            onChange={handleChange}
            rows={3}
            placeholder="Any specific concerns or requests..."
            className="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        {/* Payment Section for Virtual Consultations */}
        {showPayment && paymentSettings && isAuthenticated && (
          <div className="border-t border-slate-200 pt-6">
            <PayPalCheckoutIsolated
              amount={paymentSettings.virtualConsultationFee}
              currency={paymentSettings.currency}
              patientId={user?.id || 0}
              doctorId={parseInt(formData.doctorId)}
              serviceId={1} // You may need to get this from the selected service
              slotId={formData.slot}
              onSuccess={handlePaymentSuccess}
              onError={handlePaymentError}
              onCancel={handlePaymentCancel}
            />
          </div>
        )}

        {/* Submit Button */}
        {!showPayment && (
          <button
            type="submit"
            disabled={loading || !formData.slot}
            className="w-full py-4 bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-semibold rounded-lg hover:from-blue-700 hover:to-cyan-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg hover:shadow-xl transform hover:-translate-y-0.5"
          >
            {loading ? (locale === 'ar' ? 'جاري المعالجة...' : 'Processing...') : 
             formData.consultationType === 'virtual' && paymentSettings && isAuthenticated && paymentSettings.virtualConsultationFee
               ? (locale === 'ar' 
                   ? `ادفع ${paymentSettings.currency} ${paymentSettings.virtualConsultationFee.toFixed(2)} واحجز`
                   : `Pay ${paymentSettings.currency} ${paymentSettings.virtualConsultationFee.toFixed(2)} & Book`)
               : (locale === 'ar' ? 'تأكيد الحجز' : 'Confirm Booking')}
          </button>
        )}

        {/* Back Button for Payment Flow */}
        {showPayment && (
          <button
            type="button"
            onClick={() => setShowPayment(false)}
            className="w-full py-3 bg-slate-200 text-slate-700 font-semibold rounded-lg hover:bg-slate-300 transition-all"
          >
            {locale === 'ar' ? 'العودة إلى تفاصيل الحجز' : 'Back to Booking Details'}
          </button>
        )}
      </form>
    </div>
  );
}
