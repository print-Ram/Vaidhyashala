import React from "react";
import { Card } from "./ui/card";

export default function FormWrapper({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <Card className="mx-auto mt-5 mb-5 w-[min(500px,90%)]">{children}</Card>
  );
}
