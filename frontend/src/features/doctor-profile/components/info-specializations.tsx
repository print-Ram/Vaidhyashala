import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import { Suspense } from "react";
import { getDoctorInformation } from "../get-doctor-queries";

export default function InfoSpecializations() {
  const doctorId = "1f13bfe1-71fc-42fd-b646-8732b396f04d";
  return (
    <>
      <Card className="border-slate-800/80 shadow-md">
        <CardHeader>
          <CardTitle className="text-lg font-sans">Specializations</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="flex flex-wrap gap-2.5">
            <Suspense
              fallback={
                <>
                  <SpecializationSlotSkeleton />
                  <SpecializationSlotSkeleton />
                </>
              }
            >
              <SpecializationsList doctorId={doctorId} />
            </Suspense>
          </ul>
        </CardContent>
      </Card>
    </>
  );
}

function SpecializationSlot({ title }: { title: string }) {
  return (
    <li className="bg-indigo-50 border border-indigo-100 text-indigo-700 font-mono text-xs px-3.5 py-1.5 rounded-full hover:bg-indigo-100/60 transition-colors cursor-default font-medium">
      {title}
    </li>
  );
}

function SpecializationSlotSkeleton({ className }: { className?: string }) {
  return (
    <li>
      <Skeleton
        className={cn("h-7 w-25 rounded-full bg-gray-300", className)}
      />
    </li>
  );
}

async function SpecializationsList({ doctorId }: { doctorId: string }) {
  const doctorData = await getDoctorInformation(doctorId);
  return (
    <>
      {doctorData?.specialization.map((spec, i) => {
        return <SpecializationSlot key={i} title={spec} />;
      })}
    </>
  );
}
