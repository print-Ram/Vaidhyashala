import React from "react";
import AuthProviderClient from "./auth-provider-client";

export default async function AuthProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  return <AuthProviderClient>{children}</AuthProviderClient>;
}
