import { cn } from "@/lib/utils";
import { Button } from "./ui/button";
import { PropsWithChildren } from "react";
import { ArrowLeft } from "lucide-react";
import Link from "next/link";

export default function HomeLink({
  className,
  children,
  ...props
}: React.ComponentProps<"button"> & PropsWithChildren<{ className?: string }>) {
  return (
    <Link
      href="/login"
      className="flex items-center gap-1.5 text-xs font-mono text-indigo-600 hover:text-indigo-700 hover:bg-indigo-50 border border-indigo-200 px-4 py-2 rounded-lg transition-all"
    >
      <ArrowLeft className="w-3.5 h-3.5" /> Doctor Profile
    </Link>
  );
}
