"use client";

import React from "react";

type SlotButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  isSelected: boolean;
};

export default function SlotButton({
  children,
  isSelected,
  disabled,
  ...props
}: SlotButtonProps) {
  return (
    <button
      {...props}
      disabled={disabled}
      className={`py-3 px-3 rounded-lg border text-sm font-medium transition-all flex flex-col items-center justify-center gap-1 ${
        disabled
          ? "bg-slate-100 border-slate-200 text-slate-600 opacity-60 cursor-not-allowed"
          : isSelected
            ? "bg-black/80 text-white shadow-md shadow-indigo-100 cursor-pointer"
            : "bg-white border-slate-200 text-slate-600 hover:bg-slate-50 hover:border-slate-300 aria-invalid:border-red-300 cursor-pointer"
      }`}
    >
      {children}
    </button>
  );
}
