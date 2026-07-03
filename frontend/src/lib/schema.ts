import { z } from "zod";

/**
 * ---- LOGIN SCHEMA ----
 * Mirrors fields: email, password
 */

export const signInSchema = z.object({
  email: z.string().email("Enter a valid email address."),
  password: z.string().min(8, "Password must be at least 8 characters."),
});

export type SignInFormValues = z.infer<typeof signInSchema>;

/**
 * ---- REGISTER SCHEMA ----
 * Mirrors fields: firstName, lastName, email, phoneNumber,
 * password, dateOfBirth, gender, and address details.
 */

export const signUpSchema = z.object({
  firstName: z.string().min(3, "First name must be at least 3 characters."),
  lastName: z.string().min(3, "Last name must be at least 3 characters."),
  email: z.string().email("Enter a valid email address."),
  phoneNumber: z.string().min(10, "Phone number must be at least 10 digits."),
  password: z.string().min(8, "Password must be at least 8 characters."),
  dateOfBirth: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Enter a valid date of birth (YYYY-MM-DD)"),
  gender: z.enum(["MALE", "FEMALE", "OTHER"]),
  streetAddress: z.string().min(1, "Street address is required."),
  city: z.string().min(1, "City is required."),
  state: z.string().min(1, "State is required."),
  postalCode: z.string().min(1, "Postal code is required."),
  country: z.string().min(1, "Country is required."),
});

export type SignUpFormValues = z.infer<typeof signUpSchema>;
