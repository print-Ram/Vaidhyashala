import { cn } from "@/lib/utils";
import Link from "next/link";

export default function Logo({ className }: { className?: string }) {
  return (
    <Link
      href="/"
      className={cn(
        "font-(family-name:--typography-label-mono-font-family) text-(length:--typography-headline-md-font-size) font-(--typography-headline-xl-font-weight)",
        className,
      )}
    >
      Vaidhyashala
    </Link>
  );
}
