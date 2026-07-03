import { cn } from "@/lib/utils";
import Link from "next/link";

export default function Logo({ className }: { className?: string }) {
  return (
    <Link
      href="/"
      className={cn(
        "flex flex-col items-start leading-none group cursor-pointer",
        className,
      )}
    >
      <span className="font-[family-name:var(--font-brand)] text-2xl font-extrabold text-indigo-600 tracking-wider transition-all group-hover:text-indigo-700 uppercase">
        Vaidhyashala
      </span>
      <span className="text-[9px] font-mono tracking-widest text-slate-400 uppercase mt-1.5 font-medium">
        Authentic Care, Simplified
      </span>
    </Link>
  );
}
