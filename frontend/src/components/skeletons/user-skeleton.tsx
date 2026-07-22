import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

export default function UserSkeleton({ className }: { className?: string }) {
  return (
    <>
      <Skeleton className={cn("h-8 w-20 bg-gray-300", className)} />
    </>
  );
}
