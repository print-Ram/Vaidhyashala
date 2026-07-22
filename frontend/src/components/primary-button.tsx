import { MoveRight } from "lucide-react";
import { Button } from "./ui/button";
import React from "react";
import { cn } from "@/lib/utils";

interface PrimaryButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
}

export default function PrimaryButton({
  children,
  // className,
  ...props
}: PrimaryButtonProps) {
  const { className, ...rest } = props;
  return (
    <Button
      className={cn("h-auto group w-full py-2.5 cursor-pointer", className)}
      {...rest}
    >
      <>
        {children}
        <MoveRight
          size={14}
          className="h-4 w-4 transition-transform duration-100 ease-out group-hover:translate-x-1"
        />
      </>
    </Button>
  );
}
