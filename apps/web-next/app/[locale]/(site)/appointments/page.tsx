'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { format } from 'date-fns';
import { useLocale } from 'next-intl';
import { api } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import type { Doctor, Service, AvailabilitySlot } from '@/lib/types';

export default function AppointmentsPage() {
  const router = useRouter();
  const locale = useLocale();
  const { user, isAuthenticated } = useAuth();

  const [bookingMode, setBookingMode] = useState<'CLINIC_VISIT' | 'VIRTUAL_CONSULTATION'>('CLINIC_VISIT');
  const [services, setServices] = useState<Service[]>([]);
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [selectedServiceSlug, setSelectedServiceSlug] = useState('');
  const [selectedDoctorId, setSelectedDoctorId] = useState('');
  const [selectedDate, setSelectedDate] = useState('');
  const [slots, setSlots] = useState<AvailabilitySlot[]>([]);
  const [selectedSlot, setSelectedSlot] = useState<AvailabilitySlot | null>(null);
  const [loading, setLoading] = useState(false);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [servicesData, doctorsData] = await Promise.all([
          api.getServices(locale),
          api.getDoctors(locale),
        ]);
        setServices(servicesData);
        setDoctors(doctorsData);

        if (servicesData.length > 0) setSelectedServiceSlug(servicesData[0].slug);
        if (doctorsData.length > 0) setSelectedDoctorId(doctorsData[0].id.toString());
      } catch (err: any) {
        setError(err.message || 'Failed to load booking options');
      }
    };

    fetchData();
  }, [locale]);

  useEffect(() => {
    if (selectedDoctorId && selectedServiceSlug && selectedDate) {
      fetchAvailabilitySlots();
    }
  }, [selectedDoctorId, selectedServiceSlug, selectedDate]);

  const fetchAvailabilitySlots = async () => {
    setSlotsLoading(true);
    setSlots([]);
    setSelectedSlot(null);

    try {
      const data = await api.getAvailabilitySlots(
        parseInt(selectedDoctorId),
        selectedServiceSlug,
        selectedDate
      );
      setSlots(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load availability slots');
    } finally {
      setSlotsLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!isAuthenticated || !user) {
      router.push('/login');
      return;
    }

    if (!selectedSlot) {
      setError('Please select a time slot');
      return;
    }

    setLoading(true);

    try {
      await api.createBooking({
        doctorId: parseInt(selectedDoctorId),
        serviceSlug: selectedServiceSlug,
        slot: selectedSlot.start,
        bookingMode,
      });

      setSuccess('Appointment booked successfully!');

      setTimeout(() => {
        router.push('/dashboard');
      }, 1500);
    } catch (err: any) {
      setError(err.message || 'Failed to book appointment');
    } finally {
      setLoading(false);
    }
  };

  const formatSlotTime = (slot: AvailabilitySlot) => {
    const start = new Date(slot.start);
    const end = new Date(slot.end);
    return `${format(start, 'HH:mm')} - ${format(end, 'HH:mm')}`;
  };

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-950">
      {/* Header Section */}
      <section className="py-16 bg-gradient-to-r from-blue-600 to-cyan-600">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto">
            <h1 className="text-4xl md:text-5xl font-bold text-white">Book Your Appointment</h1>
            <p className="text-blue-50 mt-4 text-lg">
              Schedule your visit to Qadri's Clinic - Choose your service, doctor, and preferred time
            </p>
          </div>
        </div>
      </section>

      {/* Booking Form Section */}
      <section className="py-16">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          {error && (
            <div className="mb-6 p-4 rounded-2xl bg-red-50 dark:bg-red-950/50 border-2 border-red-200 dark:border-red-800 text-red-700 dark:text-red-300">
              <div className="flex items-center gap-3">
                <svg className="w-6 h-6 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span className="font-semibold">{error}</span>
              </div>
            </div>
          )}

          {success && (
            <div className="mb-6 p-4 rounded-2xl bg-green-50 dark:bg-green-950/50 border-2 border-green-200 dark:border-green-800 text-green-700 dark:text-green-300">
              <div className="flex items-center gap-3">
                <svg className="w-6 h-6 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span className="font-semibold">{success}</span>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="grid lg:grid-cols-3 gap-8">
            {/* Booking Options Sidebar */}
            <div className="lg:col-span-1">
              <div className="sticky top-6 bg-white dark:bg-slate-800 rounded-2xl p-6 border border-slate-200 dark:border-slate-700 shadow-lg">
                <h2 className="text-xl font-bold text-slate-900 dark:text-slate-100 mb-6 flex items-center gap-2">
                  <svg className="w-6 h-6 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
                  </svg>
                  Booking Details
                </h2>

                <div className="space-y-5">
                  <div>
                    <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Visit Type</label>
                    <select
                      value={bookingMode}
                      onChange={(e) => setBookingMode(e.target.value as any)}
                      className="w-full rounded-xl border-2 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                    >
                      <option value="CLINIC_VISIT">🏥 In-Person Visit</option>
                      <option value="VIRTUAL_CONSULTATION">💻 Virtual Consultation</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Service</label>
                    <select
                      value={selectedServiceSlug}
                      onChange={(e) => setSelectedServiceSlug(e.target.value)}
                      className="w-full rounded-xl border-2 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                    >
                      {services.map((service) => (
                        <option key={service.slug} value={service.slug}>
                          {service.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Doctor</label>
                    <select
                      value={selectedDoctorId}
                      onChange={(e) => setSelectedDoctorId(e.target.value)}
                      className="w-full rounded-xl border-2 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                    >
                      {doctors.map((doctor) => (
                        <option key={doctor.id} value={doctor.id}>
                          {doctor.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Date</label>
                    <input
                      type="date"
                      value={selectedDate}
                      onChange={(e) => setSelectedDate(e.target.value)}
                      min={format(new Date(), 'yyyy-MM-dd')}
                      className="w-full rounded-xl border-2 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                      required
                    />
                  </div>

                  {selectedSlot && (
                    <div className="p-4 rounded-xl bg-gradient-to-br from-blue-50 to-cyan-50 dark:from-blue-950/30 dark:to-cyan-950/30 border border-blue-200 dark:border-blue-800">
                      <p className="text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1">Selected Time</p>
                      <p className="text-lg font-bold text-blue-700 dark:text-blue-400">{formatSlotTime(selectedSlot)}</p>
                    </div>
                  )}

                  <button
                    type="submit"
                    disabled={loading || !selectedSlot}
                    className="w-full rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 text-white py-4 font-bold text-lg hover:from-blue-700 hover:to-cyan-700 transition-all shadow-lg hover:shadow-xl disabled:opacity-50 disabled:cursor-not-allowed transform hover:-translate-y-0.5"
                  >
                    {loading ? (
                      <span className="flex items-center justify-center gap-2">
                        <div className="inline-block animate-spin rounded-full h-5 w-5 border-2 border-white border-t-transparent"></div>
                        Booking...
                      </span>
                    ) : (
                      <span className="flex items-center justify-center gap-2">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        Confirm Booking
                      </span>
                    )}
                  </button>
                </div>
              </div>
            </div>

            {/* Available Slots Section */}
            <div className="lg:col-span-2">
              <div className="bg-white dark:bg-slate-800 rounded-2xl p-8 border border-slate-200 dark:border-slate-700 shadow-lg min-h-[400px]">
                <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100 mb-6 flex items-center gap-2">
                  <svg className="w-6 h-6 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Available Time Slots
                </h2>

                {!selectedDate ? (
                  <div className="text-center py-16">
                    <svg className="w-20 h-20 mx-auto text-slate-300 dark:text-slate-600 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <p className="text-slate-600 dark:text-slate-300 text-lg font-medium">Please select a date to view available time slots</p>
                  </div>
                ) : slotsLoading ? (
                  <div className="text-center py-16">
                    <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-600 border-t-transparent mb-4"></div>
                    <p className="text-slate-600 dark:text-slate-300 font-medium">Loading available slots...</p>
                  </div>
                ) : slots.length === 0 ? (
                  <div className="text-center py-16">
                    <svg className="w-20 h-20 mx-auto text-slate-300 dark:text-slate-600 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <p className="text-slate-600 dark:text-slate-300 text-lg font-medium">No slots available for the selected date</p>
                    <p className="text-slate-500 dark:text-slate-400 mt-2">Please try a different date or contact us for assistance</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                    {slots.map((slot, index) => (
                      <button
                        key={index}
                        type="button"
                        onClick={() => setSelectedSlot(slot)}
                        className={`group p-4 rounded-xl border-2 font-semibold transition-all ${
                          selectedSlot === slot
                            ? 'bg-gradient-to-br from-blue-600 to-cyan-600 text-white border-blue-600 shadow-lg scale-105'
                            : 'bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 border-slate-200 dark:border-slate-700 hover:border-blue-400 dark:hover:border-blue-500 hover:shadow-md hover:scale-105'
                        }`}
                      >
                        <div className="flex flex-col items-center gap-2">
                          <svg className={`w-5 h-5 ${selectedSlot === slot ? 'text-white' : 'text-blue-600 dark:text-blue-400'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                          </svg>
                          <span className="text-sm">{formatSlotTime(slot)}</span>
                        </div>
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {/* Info Cards */}
              <div className="mt-8 grid sm:grid-cols-2 gap-6">
                <div className="p-6 rounded-2xl bg-gradient-to-br from-blue-50 to-cyan-50 dark:from-blue-950/30 dark:to-cyan-950/30 border border-blue-100 dark:border-blue-900/50">
                  <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-xl bg-blue-600 dark:bg-blue-500 flex items-center justify-center flex-shrink-0">
                      <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                    </div>
                    <div>
                      <h3 className="font-bold text-slate-900 dark:text-slate-100 mb-1">Booking Policy</h3>
                      <p className="text-sm text-slate-600 dark:text-slate-300">Please arrive 10 minutes early for your appointment. Cancellations must be made 24 hours in advance.</p>
                    </div>
                  </div>
                </div>

                <div className="p-6 rounded-2xl bg-gradient-to-br from-blue-50 to-cyan-50 dark:from-blue-950/30 dark:to-cyan-950/30 border border-blue-100 dark:border-blue-900/50">
                  <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-xl bg-cyan-600 dark:bg-cyan-500 flex items-center justify-center flex-shrink-0">
                      <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                      </svg>
                    </div>
                    <div>
                      <h3 className="font-bold text-slate-900 dark:text-slate-100 mb-1">Need Help?</h3>
                      <p className="text-sm text-slate-600 dark:text-slate-300">Contact us at +962 7 0000 0000 for assistance with booking or any questions.</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </form>
        </div>
      </section>
    </main>
  );
}
