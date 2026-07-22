import {
  AppointmentFormValues,
  BookAppointmentPayload,
  BookAppointmentPayloadSchema,
} from "@/lib/schema";
import { addMinutes, format } from "date-fns";

const APPOINTMENT_DURATION_MINUTES = 30;

function combineDateAndTime(date: Date, time: string): Date {
  const [hours, minutes, seconds] = time.split(":").map(Number);
  const combined = new Date(date);
  combined.setHours(hours, minutes, seconds ?? 0, 0);
  return combined;
}

function toLocalIsoString(date: Date): string {
  return format(date, "yyyy-MM-dd'T'HH:mm:ss");
}

export function buildAppointmentPayload(
  values: AppointmentFormValues,
  extra: {
    providerId: string;
    consultationFee: number;
    offerPercent?: number;
  },
): BookAppointmentPayload {
  const start = combineDateAndTime(values.date, values.time);
  const end = addMinutes(start, APPOINTMENT_DURATION_MINUTES);

  return BookAppointmentPayloadSchema.parse({
    providerId: extra.providerId,
    startTime: toLocalIsoString(start),
    endTime: toLocalIsoString(end),
    description: values.description,
    consultationFee: extra.consultationFee,
    offerPercent: extra.offerPercent ?? 0,
  });
}
