"use client";

import PrimaryButton from "@/components/primary-button";
import { Field, FieldLabel } from "@/components/ui/field";
import { Textarea } from "@/components/ui/textarea";
import { appointmentFormSchema, AppointmentFormValues } from "@/lib/schema";
import { APPOINTMENT_ENDPOINT } from "@/lib/secrets";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";
import { buildAppointmentPayload } from "../book-appointment-queries";
import AppointmentDatePicker from "./appointment-date-picker";
import SlotButtonGrid from "./slot-button-grid";

export default function AppointmentForm({
  token,
  providerId,
}: {
  token: string | undefined;
  providerId: string;
}) {
  const router = useRouter();

  const {
    register,
    control,
    handleSubmit,
    formState: { isSubmitting },
  } = useForm<AppointmentFormValues>({
    resolver: zodResolver(appointmentFormSchema),
    defaultValues: {
      date: undefined,
      time: "",
      description: "",
    },
  });

  const onSubmit = async (data: AppointmentFormValues) => {
    if (!token) {
      return router.push("/login");
    }
    const payload = buildAppointmentPayload(data, {
      providerId,
      consultationFee: 500,
      offerPercent: 0,
    });
    try {
      const response = await fetch(APPOINTMENT_ENDPOINT, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error(`${response.status}`);
      }

      router.push("/appointments");
    } catch (err) {
      if (err instanceof Error) {
        console.log(err);
        toast.error(err.message);
      } else {
        toast.error("Book Appointment failed");
      }
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6" noValidate>
      <Controller
        name="date"
        control={control}
        render={({ field, fieldState }) => (
          <AppointmentDatePicker
            value={field.value}
            onChange={field.onChange}
            error={fieldState.error}
          />
        )}
      />

      <Controller
        name="time"
        control={control}
        render={({ field, fieldState }) => (
          <SlotButtonGrid
            value={field.value}
            onChange={field.onChange}
            error={fieldState.error}
          />
        )}
      />

      <Field>
        <FieldLabel htmlFor="description" className="font-mono uppercase">
          Reason for Consultation{" "}
          <span className="normal-case font-sans text-xs text-slate-400">
            (optional)
          </span>
        </FieldLabel>
        <Textarea
          id="description"
          {...register("description")}
          placeholder="Reason for consultation..."
          className="min-h-28"
        />
      </Field>

      <PrimaryButton type="submit" disabled={isSubmitting}>
        Confirm Booking
      </PrimaryButton>
    </form>
  );
}
