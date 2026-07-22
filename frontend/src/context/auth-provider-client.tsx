"use client";

import React from "react";

type AuthContextType = {};

const AuthContext = React.createContext<AuthContextType | null>(null);

export default function AuthProviderClient({
  children,
}: {
  children: React.ReactNode;
}) {
  return <AuthContext value={{}}>{children}</AuthContext>;
}

export const useAuthContext = () => {
  const context = React.use(AuthContext);

  if (!context) {
    throw new Error("AuthContext must be used within it's provider");
  }

  return context;
};
