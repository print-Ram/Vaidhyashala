import LoginHeader from "@/features/login-signup-form/components/login-header";
import React from "react";

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <>
      <LoginHeader />
      {children}
    </>
  );
}
