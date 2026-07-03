"use client";

import AuthHeader from "@/components/auth-header";
import AuthSwitchContainer from "@/components/auth-switch-container";
import AuthSwitchLink from "@/components/auth-switch-link";
import FormHeaderTitle from "@/components/form-header-title";
import FormSeparator from "@/components/form-separator";
import FormWrapper from "@/components/form-wrapper";
import GoogleButton from "@/components/google-button";
import { PasswordInput } from "@/components/password-input";
import PrimaryButton from "@/components/primary-button";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { CardContent, CardHeader } from "@/components/ui/card";
import {
  Field,
  FieldContent,
  FieldError,
  FieldGroup,
  FieldLabel,
  FieldSet,
  FieldTitle,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { SignUpFormValues, signUpSchema } from "@/lib/schema";
import { REGISTRATION_ENDPOINT, GOOGLE_LOGIN_ENDPOINT } from "@/lib/secrets";
import { useGoogleLogin } from "@react-oauth/google";
import { zodResolver } from "@hookform/resolvers/zod";
import React, { Suspense } from "react";
import { Controller, useForm } from "react-hook-form";
import { useRouter, useSearchParams } from "next/navigation";

function SignUpForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirectUrl = searchParams.get("redirect") || "/";

  const [step, setStep] = React.useState(1);
  const [isSubmitting, setIsSubmitting] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [success, setSuccess] = React.useState<string | null>(null);
  const [googleAutofilled, setGoogleAutofilled] = React.useState(false);

  const { handleSubmit, control, formState, trigger, setValue } = useForm<SignUpFormValues>({
    resolver: zodResolver(signUpSchema),
    defaultValues: {
      email: "",
      password: "",
      dateOfBirth: "",
      gender: undefined,
      firstName: "",
      lastName: "",
      phoneNumber: "",
      streetAddress: "",
      city: "",
      state: "",
      postalCode: "",
      country: "India", // Default country
    },
  });

  const [open, setOpen] = React.useState(false);
  const [date, setDate] = React.useState<Date | undefined>(undefined);

  // Auto-fill query params from Google redirection
  React.useEffect(() => {
    const emailParam = searchParams.get("email");
    const firstNameParam = searchParams.get("firstName");
    const lastNameParam = searchParams.get("lastName");

    if (emailParam) {
      setValue("email", emailParam);
      setGoogleAutofilled(true);
    }
    if (firstNameParam) setValue("firstName", firstNameParam);
    if (lastNameParam) setValue("lastName", lastNameParam);
  }, [searchParams, setValue]);

  const handleGoogleLogin = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      setIsSubmitting(true);
      setError(null);
      setSuccess(null);
      try {
        const userinfoRes = await fetch("https://www.googleapis.com/oauth2/v3/userinfo", {
          headers: {
            Authorization: `Bearer ${tokenResponse.access_token}`,
          },
        });
        
        if (!userinfoRes.ok) {
          throw new Error("Failed to retrieve profile information from Google.");
        }

        const profile = await userinfoRes.json();
        
        // Auto-fill fields immediately in the form
        if (profile.email) setValue("email", profile.email);
        if (profile.given_name) setValue("firstName", profile.given_name);
        if (profile.family_name) setValue("lastName", profile.family_name);
        setGoogleAutofilled(true);
        setSuccess("Successfully loaded your profile info from Google!");

        // Check if user is already registered in DB
        const response = await fetch(GOOGLE_LOGIN_ENDPOINT, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ email: profile.email }),
        });

        if (response.ok) {
          const result = await response.json();
          // User already exists -> log them in
          localStorage.setItem("accessToken", result.accessToken);
          localStorage.setItem("refreshToken", result.refreshToken);
          setSuccess("Google account already registered! Logging you in...");
          setTimeout(() => {
            router.push(redirectUrl);
          }, 1500);
        }
      } catch (err: any) {
        console.error("Google auto-fill error:", err);
        setError("Could not retrieve Google profile details.");
      } finally {
        setIsSubmitting(false);
      }
    },
    onError: (errResponse) => {
      console.error("Google Auth error:", errResponse);
      setError("Google authentication failed.");
    }
  });

  const handleNextStep = async (e: React.MouseEvent) => {
    e.preventDefault();
    const isValid = await trigger([
      "firstName",
      "lastName",
      "email",
      "password",
      "phoneNumber",
      "gender",
      "dateOfBirth",
    ]);
    if (isValid) {
      setError(null);
      setStep(2);
    } else {
      setError("Please resolve form validation errors before proceeding.");
    }
  };

  const onSubmit = async (data: SignUpFormValues) => {
    setIsSubmitting(true);
    setError(null);
    setSuccess(null);
    try {
      const response = await fetch(REGISTRATION_ENDPOINT, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || "Registration failed. Please check your credentials.");
      }

      setSuccess("Account registered successfully! Redirecting to login...");
      setTimeout(() => {
        router.push(`/login${redirectUrl !== "/" ? `?redirect=${encodeURIComponent(redirectUrl)}` : ""}`);
      }, 2000);
    } catch (err: any) {
      console.error("Registration error:", err);
      setError(err.message || "An unexpected error occurred during signup.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const genders = [
    { id: "MALE", title: "Male" },
    { id: "FEMALE", title: "Female" },
    { id: "OTHER", title: "Other" },
  ] as const;

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-8 text-slate-800">
      <AuthHeader />
      <FormWrapper className="mx-auto w-full max-w-md transition-all duration-500 ease-in-out transform scale-100 hover:scale-[1.01] shadow-xl border border-slate-200 bg-white">
        <CardHeader>
          <div className="flex justify-between items-center mb-2">
            <FormHeaderTitle>Create your account</FormHeaderTitle>
            <div className="flex items-center gap-1.5 font-mono text-xs text-indigo-700 bg-indigo-50 border border-indigo-100 px-2 py-0.5 rounded">
              <span>STEP {step}/2</span>
            </div>
          </div>
          <AuthSwitchContainer>
            Already with us?{" "}
            <AuthSwitchLink href={`/login${redirectUrl !== "/" ? `?redirect=${encodeURIComponent(redirectUrl)}` : ""}`}>Sign in instead</AuthSwitchLink>
          </AuthSwitchContainer>
          
          {/* Custom Animated Progress Bar */}
          <div className="w-full bg-slate-100 h-1.5 rounded-full mt-4 overflow-hidden border border-slate-200/60">
            <div 
              className="bg-gradient-to-r from-indigo-500 to-indigo-600 h-full rounded-full transition-all duration-500 ease-in-out"
              style={{ width: step === 1 ? "50%" : "100%" }}
            />
          </div>
        </CardHeader>
        <CardContent className="relative overflow-hidden">
          {googleAutofilled && (
            <div className="mb-4 p-3 bg-indigo-50 border border-indigo-100 rounded-lg text-indigo-700 text-xs font-semibold flex gap-2 items-center animate-fade-in shadow-sm">
              ✨ Profile pre-filled from Google. Create a password to finish signup!
            </div>
          )}
          {step === 1 && !googleAutofilled && (
            <div className="mb-5">
              <GoogleButton type="button" onClick={() => handleGoogleLogin()}>
                Sign up with Google
              </GoogleButton>
              <FormSeparator />
            </div>
          )}

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm animate-fade-in">
              ⚠️ {error}
            </div>
          )}
          {success && (
            <div className="mb-4 p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-700 text-sm animate-fade-in">
              ✅ {success}
            </div>
          )}

          <form id="signup-form" onSubmit={handleSubmit(onSubmit)}>
            <div 
              className="relative w-full transition-all duration-500 ease-in-out" 
              style={{ height: step === 1 ? "680px" : "460px" }}
            >
              {/* STEP 1: Personal Details */}
              <div 
                className={`absolute top-0 left-0 w-full transition-all duration-500 ease-in-out ${
                  step === 1 
                    ? "translate-x-0 opacity-100" 
                    : "-translate-x-full opacity-0 pointer-events-none"
                }`}
              >
                <FieldGroup>
                  <div className="grid grid-cols-2 gap-4">
                    <Controller
                      name="firstName"
                      control={control}
                      render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                          <FieldLabel className="font-mono text-xs" htmlFor="firstName">
                            FIRST NAME
                          </FieldLabel>
                          <Input
                            {...field}
                            id="firstName"
                            aria-invalid={fieldState.invalid}
                            placeholder="First Name"
                            autoComplete="off"
                            className="focus:ring-2 focus:ring-indigo-500/20"
                          />
                          {fieldState.invalid && (
                            <FieldError errors={[fieldState.error]} />
                          )}
                        </Field>
                      )}
                    />
                    <Controller
                      name="lastName"
                      control={control}
                      render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                          <FieldLabel className="font-mono text-xs" htmlFor="lastName">
                            LAST NAME
                          </FieldLabel>
                          <Input
                            {...field}
                            id="lastName"
                            aria-invalid={fieldState.invalid}
                            placeholder="Last Name"
                            autoComplete="off"
                            className="focus:ring-2 focus:ring-indigo-500/20"
                          />
                          {fieldState.invalid && (
                            <FieldError errors={[fieldState.error]} />
                          )}
                        </Field>
                      )}
                    />
                  </div>
                  <Controller
                    name="email"
                    control={control}
                    render={({ field, fieldState }) => (
                      <Field data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor="email" className="font-mono text-xs">
                          EMAIL ADDRESS
                        </FieldLabel>
                        <Input
                          {...field}
                          id="email"
                          aria-invalid={fieldState.invalid}
                          placeholder="name@example.com"
                          autoComplete="off"
                          className="focus:ring-2 focus:ring-indigo-500/20"
                        />
                        {fieldState.invalid && (
                          <FieldError errors={[fieldState.error]} />
                        )}
                      </Field>
                    )}
                  />
                  <Controller
                    name="password"
                    control={control}
                    render={({ field, fieldState }) => (
                      <Field data-invalid={fieldState.invalid}>
                        <FieldLabel className="font-mono text-xs" htmlFor="password">
                          PASSWORD
                        </FieldLabel>
                        <PasswordInput
                          {...field}
                          id="password"
                          aria-invalid={fieldState.invalid}
                          autoComplete="off"
                          placeholder="Min 8 characters"
                        />
                        {fieldState.invalid && (
                          <FieldError errors={[fieldState.error]} />
                        )}
                      </Field>
                    )}
                  />
                  <Controller
                    name="phoneNumber"
                    control={control}
                    render={({ field, fieldState }) => (
                      <Field data-invalid={fieldState.invalid}>
                        <FieldLabel className="font-mono text-xs" htmlFor="phoneNumber">
                          PHONE NUMBER
                        </FieldLabel>
                        <Input
                          {...field}
                          id="phoneNumber"
                          aria-invalid={fieldState.invalid}
                          placeholder="Phone Number (10 digits)"
                          autoComplete="off"
                          className="focus:ring-2 focus:ring-indigo-500/20"
                        />
                        {fieldState.invalid && (
                          <FieldError errors={[fieldState.error]} />
                        )}
                      </Field>
                    )}
                  />
                  <Controller
                    name="gender"
                    control={control}
                    render={({ field, fieldState }) => (
                      <FieldSet data-invalid={fieldState.invalid}>
                        <FieldLabel className="font-mono text-xs">GENDER</FieldLabel>
                        <RadioGroup
                          name={field.name}
                          value={field.value || ""}
                          onValueChange={field.onChange}
                          aria-invalid={fieldState.invalid}
                          className="grid grid-cols-3 gap-4 mt-2"
                        >
                          {genders.map((g) => (
                            <FieldLabel
                              key={g.id}
                              htmlFor={`gender-${g.id}`}
                              className="cursor-pointer border border-slate-800 rounded-lg p-2.5 flex items-center justify-between hover:bg-slate-900/60 transition-colors"
                            >
                              <Field
                                orientation="horizontal"
                                data-invalid={fieldState.invalid}
                                className="w-full justify-between"
                              >
                                <FieldContent>
                                  <FieldTitle className="text-sm font-sans">{g.title}</FieldTitle>
                                </FieldContent>
                                <RadioGroupItem
                                  value={g.id}
                                  id={`gender-${g.id}`}
                                  aria-invalid={fieldState.invalid}
                                />
                              </Field>
                            </FieldLabel>
                          ))}
                        </RadioGroup>
                        {fieldState.invalid && (
                          <FieldError errors={[fieldState.error]} />
                        )}
                      </FieldSet>
                    )}
                  />
                  <Controller
                    name="dateOfBirth"
                    control={control}
                    render={({ field, fieldState }) => (
                      <Field data-invalid={fieldState.invalid}>
                        <FieldLabel className="font-mono text-xs" htmlFor="dateOfBirth">
                          DATE OF BIRTH
                        </FieldLabel>
                        <Popover open={open} onOpenChange={setOpen}>
                          <PopoverTrigger asChild>
                            <Button
                              type="button"
                              variant="outline"
                              id="dateOfBirth"
                              aria-invalid={fieldState.invalid}
                              className="justify-start font-normal w-full border-slate-800 hover:bg-slate-900 text-slate-300"
                            >
                              {date ? date.toLocaleDateString() : "Select date"}
                            </Button>
                          </PopoverTrigger>
                          <PopoverContent
                            className="w-auto overflow-hidden p-0 bg-slate-950 border border-slate-800"
                            align="start"
                          >
                            <Calendar
                              mode="single"
                              {...field}
                              selected={date}
                              defaultMonth={date}
                              captionLayout="dropdown"
                              onSelect={(selectedDate) => {
                                setDate(selectedDate);
                                if (selectedDate) {
                                  field.onChange(
                                    selectedDate.toISOString().split("T")[0],
                                  );
                                }
                                setOpen(false);
                              }}
                            />
                          </PopoverContent>
                        </Popover>
                        {fieldState.invalid && (
                          <FieldError errors={[fieldState.error]} />
                        )}
                      </Field>
                    )}
                  />
                </FieldGroup>
                <div className="mt-6">
                  <PrimaryButton onClick={handleNextStep}>Next: Address Details ➔</PrimaryButton>
                  <FormSeparator />
                  <GoogleButton>Sign up with Google</GoogleButton>
                </div>
              </div>

              {/* STEP 2: Address Details */}
              <div 
                className={`absolute top-0 left-0 w-full transition-all duration-500 ease-in-out ${
                  step === 2 
                    ? "translate-x-0 opacity-100" 
                    : "translate-x-full opacity-0 pointer-events-none"
                }`}
              >
                <FieldGroup>
                  <Controller
                    name="streetAddress"
                    control={control}
                    render={({ field, fieldState }) => (
                      <Field data-invalid={fieldState.invalid}>
                        <FieldLabel className="font-mono text-xs" htmlFor="streetAddress">
                          STREET ADDRESS
                        </FieldLabel>
                        <Input
                          {...field}
                          id="streetAddress"
                          aria-invalid={fieldState.invalid}
                          placeholder="123 Main St, Apt 4B"
                          autoComplete="off"
                          className="focus:ring-2 focus:ring-indigo-500/20"
                        />
                        {fieldState.invalid && (
                          <FieldError errors={[fieldState.error]} />
                        )}
                      </Field>
                    )}
                  />
                  <div className="grid grid-cols-2 gap-4">
                    <Controller
                      name="city"
                      control={control}
                      render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                          <FieldLabel className="font-mono text-xs" htmlFor="city">
                            CITY
                          </FieldLabel>
                          <Input
                            {...field}
                            id="city"
                            aria-invalid={fieldState.invalid}
                            placeholder="City"
                            autoComplete="off"
                            className="focus:ring-2 focus:ring-indigo-500/20"
                          />
                          {fieldState.invalid && (
                            <FieldError errors={[fieldState.error]} />
                          )}
                        </Field>
                      )}
                    />
                    <Controller
                      name="state"
                      control={control}
                      render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                          <FieldLabel className="font-mono text-xs" htmlFor="state">
                            STATE / REGION
                          </FieldLabel>
                          <Input
                            {...field}
                            id="state"
                            aria-invalid={fieldState.invalid}
                            placeholder="State"
                            autoComplete="off"
                            className="focus:ring-2 focus:ring-indigo-500/20"
                          />
                          {fieldState.invalid && (
                            <FieldError errors={[fieldState.error]} />
                          )}
                        </Field>
                      )}
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <Controller
                      name="postalCode"
                      control={control}
                      render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                          <FieldLabel className="font-mono text-xs" htmlFor="postalCode">
                            POSTAL / ZIP CODE
                          </FieldLabel>
                          <Input
                            {...field}
                            id="postalCode"
                            aria-invalid={fieldState.invalid}
                            placeholder="123456"
                            autoComplete="off"
                            className="focus:ring-2 focus:ring-indigo-500/20"
                          />
                          {fieldState.invalid && (
                            <FieldError errors={[fieldState.error]} />
                          )}
                        </Field>
                      )}
                    />
                    <Controller
                      name="country"
                      control={control}
                      render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                          <FieldLabel className="font-mono text-xs" htmlFor="country">
                            COUNTRY
                          </FieldLabel>
                          <Input
                            {...field}
                            id="country"
                            aria-invalid={fieldState.invalid}
                            placeholder="Country"
                            autoComplete="off"
                            className="focus:ring-2 focus:ring-indigo-500/20"
                          />
                          {fieldState.invalid && (
                            <FieldError errors={[fieldState.error]} />
                          )}
                        </Field>
                      )}
                    />
                  </div>
                </FieldGroup>
                
                <div className="flex gap-4 mt-8">
                  <Button 
                    type="button" 
                    variant="outline" 
                    className="w-1/3 border-slate-800 hover:bg-slate-900 hover:text-white" 
                    onClick={() => setStep(1)}
                  >
                    ⏮ Back
                  </Button>
                  <PrimaryButton 
                    type="submit" 
                    className="w-2/3 bg-indigo-600 hover:bg-indigo-700 active:bg-indigo-800"
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? "Registering..." : "Submit & Register ✅"}
                  </PrimaryButton>
                </div>
              </div>
            </div>
          </form>
        </CardContent>
      </FormWrapper>
    </div>
  );
}

export default function SignUpPage() {
  return (
    <Suspense fallback={
      <div className="flex items-center justify-center min-h-screen bg-slate-50 text-slate-500 font-mono text-sm">
        Loading Sign Up...
      </div>
    }>
      <SignUpForm />
    </Suspense>
  );
}

