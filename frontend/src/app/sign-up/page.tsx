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
import { REGISTRATION_ENDPOINT } from "@/lib/secrets";
import { zodResolver } from "@hookform/resolvers/zod";
import React from "react";
import { Controller, useForm } from "react-hook-form";

export default function SignUpPage() {
  const { handleSubmit, control, formState } = useForm<SignUpFormValues>({
    resolver: zodResolver(signUpSchema),
    defaultValues: {
      email: "",
      password: "",
      dateOfBirth: "",
      gender: undefined,
      firstName: "",
      lastName: "",
      phoneNumber: "",
    },
  });

  const [open, setOpen] = React.useState(false);
  const [date, setDate] = React.useState<Date | undefined>(undefined);

  const onSubmit = async (data: SignUpFormValues) => {
    try {
      const response = await fetch(REGISTRATION_ENDPOINT, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
        credentials: "include",
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || "Registration failed");
      }

      console.log("Registration successful:", result);
    } catch (error) {
      console.error("Registration error:", error);
    }
  };

  const genders = [
    { id: "MALE", title: "Male" },
    { id: "FEMALE", title: "Female" },
    { id: "OTHER", title: "Other" },
  ] as const;

  return (
    <>
      <AuthHeader />
      <FormWrapper>
        <CardHeader>
          <FormHeaderTitle>Create your account</FormHeaderTitle>
          <AuthSwitchContainer>
            Already with us?{" "}
            <AuthSwitchLink href="/login">Sign in instead</AuthSwitchLink>
          </AuthSwitchContainer>
        </CardHeader>
        <CardContent>
          <form id="login-form" onSubmit={handleSubmit(onSubmit)}>
            <FieldGroup>
              <Controller
                name="firstName"
                control={control}
                render={({ field, fieldState }) => (
                  <Field data-invalid={fieldState.invalid}>
                    <FieldLabel
                      className="font-mono capitalize"
                      htmlFor="firstName"
                    >
                      FIRST NAME
                    </FieldLabel>
                    <Input
                      className="placeholder:font-mono"
                      {...field}
                      id="firstName"
                      aria-invalid={fieldState.invalid}
                      placeholder="First Name"
                      autoComplete="off"
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
                    <FieldLabel
                      className="font-mono capitalize"
                      htmlFor="lastName"
                    >
                      LAST NAME
                    </FieldLabel>
                    <Input
                      className="placeholder:font-mono"
                      {...field}
                      id="lastName"
                      aria-invalid={fieldState.invalid}
                      placeholder="Last Name"
                      autoComplete="off"
                    />
                    {fieldState.invalid && (
                      <FieldError errors={[fieldState.error]} />
                    )}
                  </Field>
                )}
              />
              <Controller
                name="email"
                control={control}
                render={({ field, fieldState }) => (
                  <Field data-invalid={fieldState.invalid}>
                    <FieldLabel
                      htmlFor="email"
                      className="font-mono capitalize"
                    >
                      EMAIL ADDRESS
                    </FieldLabel>
                    <Input
                      className="placeholder:font-mono"
                      {...field}
                      id="email"
                      aria-invalid={fieldState.invalid}
                      placeholder="[EMAIL_ADDRESS]"
                      autoComplete="off"
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
                    <FieldLabel
                      className="font-mono capitalize"
                      htmlFor="password"
                    >
                      PASSWORD
                    </FieldLabel>
                    <PasswordInput
                      {...field}
                      id="password"
                      aria-invalid={fieldState.invalid}
                      autoComplete="off"
                      placeholder="********"
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
                    <FieldLabel className="font-mono" htmlFor="phoneNumber">
                      PHONE NUMBER
                    </FieldLabel>
                    <Input
                      className="placeholder:font-mono"
                      {...field}
                      id="phoneNumber"
                      aria-invalid={fieldState.invalid}
                      placeholder="Phone Number"
                      autoComplete="off"
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
                    <FieldLabel className="font-mono">GENDER</FieldLabel>
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
                          className="cursor-pointer"
                        >
                          <Field
                            orientation="horizontal"
                            data-invalid={fieldState.invalid}
                          >
                            <FieldContent>
                              <FieldTitle>{g.title}</FieldTitle>
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
                    <FieldLabel
                      className="font-mono capitalize"
                      htmlFor="dateOfBirth"
                    >
                      DATE OF BIRTH
                    </FieldLabel>
                    <Popover open={open} onOpenChange={setOpen}>
                      <PopoverTrigger asChild>
                        <Button
                          variant="outline"
                          id="dateOfBirth"
                          aria-invalid={fieldState.invalid}
                          className="justify-start font-normal"
                        >
                          {date ? date.toLocaleDateString() : "Select date"}
                        </Button>
                      </PopoverTrigger>
                      <PopoverContent
                        className="w-auto overflow-hidden p-0"
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
            <PrimaryButton>Sign up</PrimaryButton>
            <FormSeparator />
            <GoogleButton>Sign up with Google</GoogleButton>
          </form>
        </CardContent>
      </FormWrapper>
    </>
  );
}
