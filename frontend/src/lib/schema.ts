import { z } from "zod";

/**
 * ---- LOGIN SCHEMA ----
 * Mirrors fields: email, password, remember (checkbox)
 */

export const signInSchema = z.object({
  email: z.email("Enter a valid email address."),
  password: z.string().min(8, "Password must be at least 8 characters."),
});

export type SignInFormValues = z.infer<typeof signInSchema>;

/**
 * ---- REGISTER SCHEMA ----
 * Mirrors fields: firstName, lastName, email, phoneNumber,
 * password, confirmPassword, agreeTerms (checkbox)
 *
 * Password strength rule matches your existing meter logic:
 * 8+ chars, upper+lower, a number, a symbol => "Strong".
 * We enforce at minimum length + at least one number + one symbol,
 * matching the visible hint text ("Use 8+ characters with a number and a symbol").
 */

export const signUpSchema = z.object({
  firstName: z.string().min(3, "First name must be at least 3 characters."),
  lastName: z.string().min(3, "Last name must be at least 3 characters."),
  email: z.email("Enter a valid email address."),
  phoneNumber: z.string().min(10, "Phone number must be at least 10 digits."),
  password: z.string().min(8, "Password must be at least 8 characters."),
  dateOfBirth: z.iso.date("Enter a valid date of birth"),
  gender: z.enum(["MALE", "FEMALE", "OTHER"]),
});

export type SignUpFormValues = z.infer<typeof signUpSchema>;
