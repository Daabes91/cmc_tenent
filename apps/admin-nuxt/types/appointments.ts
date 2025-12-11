export type AppointmentAdmin = {
  id: number;
  patientName: string;
  doctorName: string;
  serviceName: string;
  scheduledAt: number; // Unix timestamp in seconds
  status: string;
  bookingMode: string;
  treatmentPlanId?: number | null;
  followUpVisitNumber?: number | null;
  paymentCollected?: boolean;
  patientAttended?: boolean | null;
  patientConfirmed?: boolean;
  patientConfirmedAt?: string | null;
  slotDurationMinutes?: number | null;
  paymentAmount?: number | null;
  paymentMethod?: string | null;
  paymentCurrency?: string | null;
};

export type AppointmentAdminDetail = {
  id: number;
  patient: { id: number; name: string } | null;
  doctor: { id: number; name: string } | null;
  service: { id: number; name: string } | null;
  status: string;
  bookingMode: string;
  scheduledAt: string | null;
  createdAt: string | null;
  notes: string | null;
  treatmentPlanId: number | null;
  followUpVisitNumber: number | null;
  paymentCollected: boolean;
  patientAttended: boolean | null;
  slotDurationMinutes: number | null;
  paymentAmount: number | null;
  paymentMethod: string | null;
  paymentCurrency: string | null;
  paymentDate: string | null;
  paymentReference: string | null;
  paymentNotes: string | null;
};

export type AvailabilitySlot = {
  doctorId: number;
  doctorName: string;
  start: string;
  end: string;
};
