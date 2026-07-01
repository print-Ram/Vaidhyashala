"use client";

import AuthHeader from "@/components/auth-header";
import AuthSwitchContainer from "@/components/auth-switch-container";
import AuthSwitchLink from "@/components/auth-switch-link";
import FormHeaderTitle from "@/components/form-header-title";
import FormSeparator from "@/components/form-separator";
import FormWrapper from "@/components/form-wrapper";
import GoogleButton from "@/components/google-button";
import Logo from "@/components/logo";
import { PasswordInput } from "@/components/password-input";
import PrimaryButton from "@/components/primary-button";
import { CardContent, CardHeader } from "@/components/ui/card";
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { SignInFormValues, signInSchema } from "@/lib/schema";
import { LOGIN_ENDPOINT } from "@/lib/secrets";
import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";

export default function LoginPage() {
  const { handleSubmit, control, formState } = useForm<SignInFormValues>({
    resolver: zodResolver(signInSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (data: SignInFormValues) => {
    try {
      const response = await fetch(LOGIN_ENDPOINT, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
        credentials: "include",
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || "Login failed");
      }

      console.log("Login successful:", result);
    } catch (error) {
      console.error("Login error:", error);
    }
  };

  return (
    <>
      <AuthHeader />
      <FormWrapper>
        <CardHeader>
          <FormHeaderTitle>Welcome Back</FormHeaderTitle>
          <AuthSwitchContainer>
            New to Vaidhyashala?
            <AuthSwitchLink href="/sign-up">Create an account</AuthSwitchLink>
          </AuthSwitchContainer>
        </CardHeader>
        <CardContent>
          <form id="login-form" onSubmit={handleSubmit(onSubmit)}>
            <FieldGroup>
              <Controller
                name="email"
                control={control}
                render={({ field, fieldState }) => (
                  <Field data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="email" className="font-mono">
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
                  <Field className="mb-6" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="password" className="font-mono">
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
            </FieldGroup>
            <PrimaryButton>Sign in</PrimaryButton>
            <FormSeparator />
            <GoogleButton>Sign in with Google</GoogleButton>
          </form>
        </CardContent>
      </FormWrapper>
    </>
  );
}
