"use client";

import { PasswordInput } from "@/components/password-input";
import PrimaryButton from "@/components/primary-button";
import { Button } from "@/components/ui/button";
import { CardContent, CardHeader } from "@/components/ui/card";
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import AuthSwitchContainer from "@/features/login-signup-form/components/auth-switch-container";
import AuthSwitchLink from "@/features/login-signup-form/components/auth-switch-link";
import FormHeaderTitle from "@/features/login-signup-form/components/form-header-title";
import FormSeparator from "@/features/login-signup-form/components/form-separator";
import FormWrapper from "@/features/login-signup-form/components/form-wrapper";
import GoogleButton from "@/features/login-signup-form/components/google-button";
import { SignInFormValues, signInSchema } from "@/lib/schema";
import { LOGIN_ENDPOINT } from "@/lib/secrets";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

export default function LoginPage() {
  const router = useRouter();

  const {
    handleSubmit,
    control,
    formState: { isSubmitting },
  } = useForm<SignInFormValues>({
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
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || "Invalid email or password.");
      }

      console.log(result);

      document.cookie = `accessToken=${result.accessToken}; path=/; max-age=${result.expiresIn}; SameSite=Lax`;

      router.push("/doctor");
    } catch (err: any) {
      const errorMsg = err instanceof Error ? err.message : "Login Failed";
      toast.error(errorMsg, { position: "bottom-right" });
    }
  };

  return (
    <>
      <FormWrapper>
        <CardHeader>
          <FormHeaderTitle>Welcome Back</FormHeaderTitle>
          <AuthSwitchContainer>
            New to Vaidhyashala?
            <AuthSwitchLink href={"/doctor/sign-up"}>
              Create an account
            </AuthSwitchLink>
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
                    <FieldLabel
                      htmlFor="password"
                      className="font-mono text-xs"
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
            </FieldGroup>
            {isSubmitting ? (
              <Button className="mt-5 py-2.5 h-auto w-full bg-primary/80">
                Logging in <Spinner data-icon="inline-start" />
              </Button>
            ) : (
              <PrimaryButton className="mt-5">Login</PrimaryButton>
            )}
            <FormSeparator />
            <GoogleButton type="button">Sign in with Google</GoogleButton>
          </form>
        </CardContent>
      </FormWrapper>
    </>
  );
}
