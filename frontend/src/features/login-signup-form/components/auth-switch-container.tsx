import { CardDescription } from "@/components/ui/card";
import React from "react";

export default function AuthSwitchContainer({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <CardDescription className="flex items-center gap-1 flex-col md:flex-row md:justify-center md:gap-2">
      {children}
    </CardDescription>
  );
}
