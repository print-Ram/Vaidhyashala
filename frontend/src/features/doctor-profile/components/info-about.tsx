import { cn } from "@/lib/utils";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Suspense } from "react";
import { getDoctorInformation } from "../get-doctor-queries";

export default function InfoAbout() {
  const doctorId = "1f13bfe1-71fc-42fd-b646-8732b396f04d";
  return (
    <>
      <Card className="border-slate-800/80 shadow-md">
        <CardHeader>
          <CardTitle className="text-lg font-sans">
            About Dr.{" "}
            <Suspense
              fallback={
                <TitleSkeleton className="inline-block align-middle h-7" />
              }
            >
              <DoctorName doctorId={doctorId} />
            </Suspense>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Suspense fallback={<ParagraphSkeleton />}>
            <DoctorAbout doctorId={doctorId} />
          </Suspense>
        </CardContent>
      </Card>
    </>
  );
}

async function DoctorName({ doctorId }: { doctorId: string }) {
  const doctorData = await getDoctorInformation(doctorId);
  return (
    <>
      {doctorData?.firstName} {doctorData?.lastName}
    </>
  );
}

async function DoctorAbout({ doctorId }: { doctorId: string }) {
  const doctorData = await getDoctorInformation(doctorId);
  return (
    <>
      <p className="text-sm md:text-base leading-relaxed">
        {doctorData?.about}
      </p>
    </>
  );
}

export function TitleSkeleton({ className }: { className?: string }) {
  return <Skeleton className={cn("w-56 h-8 bg-gray-300", className)} />;
}

export function ParagraphSkeleton({ className }: { className?: string }) {
  return (
    <Skeleton className={cn("h-50 w-full bg-gray-300 md:h-35", className)} />
  );
}
