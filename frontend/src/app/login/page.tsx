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
import { CardContent, CardHeader } from "@/components/ui/card";
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { SignInFormValues, signInSchema } from "@/lib/schema";
import { GOOGLE_LOGIN_ENDPOINT, LOGIN_ENDPOINT } from "@/lib/secrets";
import { useGoogleLogin } from "@react-oauth/google";
import { zodResolver } from "@hookform/resolvers/zod";
import React, { Suspense } from "react";
import { Controller, useForm } from "react-hook-form";
import { useRouter, useSearchParams } from "next/navigation";

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirectUrl = searchParams.get("redirect") || "/";
  
  const [isSubmitting, setIsSubmitting] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [success, setSuccess] = React.useState<string | null>(null);

  const { handleSubmit, control, formState } = useForm<SignInFormValues>({
    resolver: zodResolver(signInSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (data: SignInFormValues) => {
    setIsSubmitting(true);
    setError(null);
    setSuccess(null);
    try {
      const response = await fetch(LOGIN_ENDPOINT, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || "Invalid email or password.");
      }

      // Store JWT tokens securely in localStorage
      localStorage.setItem("accessToken", result.accessToken);
      localStorage.setItem("refreshToken", result.refreshToken);

      setSuccess("Login successful! Redirecting...");
      setTimeout(() => {
        router.push(redirectUrl);
      }, 1500);
    } catch (err: any) {
      console.error("Login error:", err);
      setError(err.message || "An unexpected error occurred during login.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleGoogleLogin = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      setIsSubmitting(true);
      setError(null);
      setSuccess(null);
      try {
        // Fetch user profile info from Google API
        const userinfoRes = await fetch("https://www.googleapis.com/oauth2/v3/userinfo", {
          headers: {
            Authorization: `Bearer ${tokenResponse.access_token}`,
          },
        });
        
        if (!userinfoRes.ok) {
          throw new Error("Failed to retrieve profile information from Google.");
        }

        const profile = await userinfoRes.json();
        
        // Send email to backend to authenticate/check registration
        const response = await fetch(GOOGLE_LOGIN_ENDPOINT, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ email: profile.email }),
        });

        if (response.status === 404) {
          // User is not registered. Redirect to Sign Up with autofill params.
          setSuccess("Google account authenticated! Redirecting to fill your registration details...");
          setTimeout(() => {
            const signupParams = new URLSearchParams();
            signupParams.set("email", profile.email || "");
            signupParams.set("firstName", profile.given_name || "");
            signupParams.set("lastName", profile.family_name || "");
            signupParams.set("redirect", redirectUrl);
            router.push(`/sign-up?${signupParams.toString()}`);
          }, 1500);
          return;
        }

        const result = await response.json();

        if (!response.ok) {
          throw new Error(result.message || "Google Authentication failed.");
        }

        // Store JWT tokens securely in localStorage
        localStorage.setItem("accessToken", result.accessToken);
        localStorage.setItem("refreshToken", result.refreshToken);

        setSuccess("Login successful! Redirecting...");
        setTimeout(() => {
          router.push(redirectUrl);
        }, 1500);
      } catch (err: any) {
        console.error("Google Auth error:", err);
        setError(err.message || "An unexpected error occurred during Google authentication.");
      } finally {
        setIsSubmitting(false);
      }
    },
    onError: (errorResponse) => {
      console.error("Google OAuth error:", errorResponse);
      setError("Google Sign-In was cancelled or failed.");
    }
  });

  const signUpHref = `/sign-up${redirectUrl !== "/" ? `?redirect=${encodeURIComponent(redirectUrl)}` : ""}`;

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-8 text-slate-800">
      <AuthHeader />
      <FormWrapper className="mx-auto w-full max-w-md transition-all duration-500 ease-in-out transform scale-100 hover:scale-[1.01] shadow-xl border border-slate-200 bg-white">
        <CardHeader>
          <FormHeaderTitle>Welcome Back</FormHeaderTitle>
          <AuthSwitchContainer>
            New to Vaidhyashala?
            <AuthSwitchLink href={signUpHref}>Create an account</AuthSwitchLink>
          </AuthSwitchContainer>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="mb-4 p-3 bg-red-55 border border-red-200 rounded-lg text-red-700 text-sm animate-fade-in">
              ⚠️ {error}
            </div>
          )}
          {success && (
            <div className="mb-4 p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-700 text-sm animate-fade-in">
              ✅ {success}
            </div>
          )}

          <form id="login-form" onSubmit={handleSubmit(onSubmit)}>
            <FieldGroup>
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
                  <Field className="mb-6" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="password" className="font-mono text-xs">
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
            <PrimaryButton type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Signing in..." : "Sign in"}
            </PrimaryButton>
            <FormSeparator />
            <GoogleButton type="button" onClick={() => handleGoogleLogin()}>
              Sign in with Google
            </GoogleButton>
          </form>
        </CardContent>
      </FormWrapper>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={
      <div className="flex items-center justify-center min-h-screen bg-slate-50 text-slate-500 font-mono text-sm">
        Loading Login...
      </div>
    }>
      <LoginForm />
    </Suspense>
  );
}


