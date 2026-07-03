import React from "react";
import { Card } from "./ui/card";
import { cn } from "@/lib/utils";

export default function FormWrapper({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <Card className={cn("mx-auto mt-5 mb-5 w-[min(500px,90%)]", className)}>
      {children}
    </Card>
  );
}

