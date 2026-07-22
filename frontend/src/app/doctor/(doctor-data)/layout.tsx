import Header from "@/components/header";
import React from "react";

export default function DoctorLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 flex flex-col font-sans">
      <Header user="DOCTOR" />
      {children}
    </div>
  );
}
