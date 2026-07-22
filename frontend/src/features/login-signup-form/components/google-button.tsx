import { Button } from "@/components/ui/button";
import React from "react";

interface GoogleButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: string;
}

export default function GoogleButton({
  children,
  className,
  ...props
}: GoogleButtonProps) {
  return (
    <Button
      className={`h-auto py-2.5 w-full transition-all duration-300 hover:shadow active:scale-[0.98] cursor-pointer flex items-center justify-center gap-2 ${className || ""}`}
      variant={"outline"}
      {...props}
    >
      <svg className="w-4 h-4" viewBox="0 0 24 24">
        <path
          fill="#4285F4"
          d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1Z"
        ></path>
        <path
          fill="#34A853"
          d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.99.67-2.26 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84A11 11 0 0 0 12 23Z"
        ></path>
        <path
          fill="#FBBC05"
          d="M5.84 14.09A6.6 6.6 0 0 1 5.5 12c0-.73.12-1.43.34-2.09V7.07H2.18A11 11 0 0 0 1 12c0 1.77.43 3.45 1.18 4.93l3.66-2.84Z"
        ></path>
        <path
          fill="#EA4335"
          d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.16-3.16C17.45 1.99 14.97 1 12 1A11 11 0 0 0 2.18 7.07l3.66 2.84C6.71 7.31 9.14 5.38 12 5.38Z"
        ></path>
      </svg>
      {children}
    </Button>
  );
}
