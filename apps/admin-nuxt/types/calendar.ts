export type CalendarTimeRange = {
  start: Date;
  end: Date;
};

export type CalendarAppointmentRequest = {
  start: Date;
  end: Date;
};

export type CalendarAppointmentPayload = {
  date: string;
  startTime: string;
  endTime: string;
  serviceId: number | null;
  doctorId: number | null;
  patientId: number | null;
  notes: string;
};

export type CalendarPeriod = {
  id: string;
  startDate: string;
  endDate: string;
  type: string;
  notes?: string | null;
  color?: string;
};

export type CalendarPeriodPayload = {
  startDate: string;
  endDate: string;
  type: string;
  notes: string;
};
