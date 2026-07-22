import { z } from "zod";

export const signInSchema = z.object({
  email: z.email("Enter a valid email address."),
  password: z.string().min(8, "Password must be at least 8 characters."),
});

export type SignInFormValues = z.infer<typeof signInSchema>;

export const signUpSchema = z.object({
  firstName: z.string().min(3, "First name must be at least 3 characters."),
  lastName: z.string().min(3, "Last name must be at least 3 characters."),
  email: z.email("Enter a valid email address."),
  phoneNumber: z.string().min(10, "Phone number must be at least 10 digits."),
  password: z.string().min(8, "Password must be at least 8 characters."),
  dateOfBirth: z
    .string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, "Enter a valid date of birth (YYYY-MM-DD)"),
  gender: z.enum(["MALE", "FEMALE", "OTHER"]),
});

export type SignUpFormValues = z.infer<typeof signUpSchema>;

export const MODES = ["video", "clinic"] as const;
export type TMode = (typeof MODES)[number];

export const DoctorSchema = z.object({
  id: z.uuid(),
  userId: z.uuid(),
  firstName: z.string(),
  lastName: z.string(),
  email: z.email(),
  phoneNumber: z.string(),
  about: z.string(),
  department: z.string(),
  status: z.string(),
  expertIn: z.array(z.string()),
  specialization: z.array(z.string()),
  educationDetails: z.array(
    z.object({
      degree: z.string(),
      institution: z.string(),
      year: z.string(),
    }),
  ),
  certifications: z.array(
    z.object({
      name: z.string(),
      issuingBody: z.string(),
      year: z.string(),
    }),
  ),
});

export type Doctor = z.infer<typeof DoctorSchema>;

export const appointmentFormSchema = z.object({
  date: z.date({
    error: "Please select a date for your appointment.",
  }),
  time: z.string().min(1, "Please select an available slot."),
  description: z.string().optional(),
});
export type AppointmentFormValues = z.infer<typeof appointmentFormSchema>;

export const BookAppointmentPayloadSchema = z.object({
  providerId: z.uuid("Invalid provider id."),
  startTime: z.iso.datetime({ local: true }),
  endTime: z.iso.datetime({ local: true }),
  description: z.string().optional().default(""),
  consultationFee: z.number().nonnegative(),
  offerPercent: z.number().min(0).max(100).default(0),
});
export type BookAppointmentPayload = z.infer<
  typeof BookAppointmentPayloadSchema
>;
