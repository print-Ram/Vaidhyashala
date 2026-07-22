import { cn } from "@/lib/utils";
import Image from "next/image";
import { Suspense } from "react";
import { Card } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { getDoctorInformation } from "../get-doctor-queries";

export default async function InfoCard() {
  const doctorId = "1f13bfe1-71fc-42fd-b646-8732b396f04d";

  return (
    <>
      <Card className="border-slate-200 bg-white shadow-sm p-6 md:p-8">
        <section className="flex flex-col md:flex-row gap-6 md:gap-8 items-center md:items-start text-center md:text-left">
          <div className="overflow-hidden rounded-full w-32.5 h-32.5 md:w-37.5 md:h-37.5 border border-slate-100 shadow-sm shrink-0 bg-slate-50">
            <Image
              src="/doctor.png"
              alt="Doctor's Image"
              width={150}
              height={150}
              className="scale-105 object-cover w-full h-full"
              loading="eager"
            />
          </div>
          <section className="flex flex-col gap-2.5 justify-center">
            {/* <span className="bg-indigo-50 border border-indigo-100 font-mono text-indigo-700 px-3.5 py-1 text-xs w-fit rounded-full uppercase tracking-wider mx-auto md:mx-0 font-medium">
              Endocrinology
            </span> */}
            <Suspense
              fallback={
                <>
                  <SpecialistTag className="mx-auto md:ml-0" />
                  <NameSkeleton className="mx-auto md:ml-0" />
                </>
              }
            >
              <DoctorName doctorId={doctorId} />
            </Suspense>
            {/* <h2 className="font-sans text-2xl md:text-3xl font-bold text-slate-800">
              Dr. Arshitha Vaidhya
            </h2> */}
            <p className="text-slate-600 text-sm md:text-base leading-relaxed">
              Senior Consultant Endocrinologist (12+ Years Experience)
            </p>
            <p className="text-slate-400 text-xs font-mono tracking-wide">
              Medical Council Reg:{" "}
              <span className="text-indigo-600 font-semibold">MCI-48921</span>
            </p>
          </section>
        </section>
      </Card>
    </>
  );
}

async function DoctorName({ doctorId }: { doctorId: string }) {
  let doctorData = await getDoctorInformation(doctorId);
  console.log(doctorData);
  return (
    <>
      <span className="bg-indigo-50 border block border-indigo-100 font-mono text-indigo-700 px-3.5 py-1 text-xs w-fit rounded-full uppercase tracking-wider mx-auto md:mx-0 font-medium">
        {doctorData?.department}
      </span>
      <h2 className="font-sans text-2xl md:text-3xl font-bold text-slate-800">
        Dr. {doctorData?.firstName} {doctorData?.lastName}
      </h2>
    </>
  );
}

export function ImageSkeleton() {
  return <Skeleton className="h-37.5 w-37.5 bg-gray-300" />;
}

export function SpecialistTag({ className }: { className?: string }) {
  return <Skeleton className={cn("w-25 h-5 bg-gray-300", className)} />;
}

export function NameSkeleton({ className }: { className?: string }) {
  return (
    <Skeleton
      className={cn("w-52 h-7 md:w-64 md:h-8 bg-gray-300", className)}
    />
  );
}

export function EducationSkeleton({ className }: { className?: string }) {
  return (
    <Skeleton
      className={cn("w-64 h-5 md:w-72 md:h-6 bg-gray-300", className)}
    />
  );
}

export function MedicalCouncilSkeleton({ className }: { className?: string }) {
  return <Skeleton className={cn("w-40 h-5 bg-gray-300", className)} />;
}
