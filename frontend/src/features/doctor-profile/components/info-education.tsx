import { cn } from "@/lib/utils";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { getDoctorInformation } from "../get-doctor-queries";
import { Suspense } from "react";

export default function InfoEducation() {
  const doctorId = "1f13bfe1-71fc-42fd-b646-8732b396f04d";
  return (
    <>
      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader>
          <CardTitle className="text-xs font-mono text-slate-500 uppercase tracking-wider">
            Education &amp; Certifications
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="relative border-l border-slate-200 ml-2.5 pl-6 space-y-6">
            <Suspense
              fallback={
                <>
                  <EducationSlotSkeleton />
                  <EducationSlotSkeleton />
                  <EducationSlotSkeleton />
                </>
              }
            >
              <EducationAndCaertificationsList doctorId={doctorId} />
            </Suspense>
          </div>
        </CardContent>
      </Card>
    </>
  );
}

function EducationSlot({
  title,
  location,
  year,
}: {
  title: string;
  location: string;
  year: string;
}) {
  return (
    <div className="relative group">
      {/* Circle Marker */}
      <div className="absolute -left-7.75 top-1.5 bg-white border-2 border-indigo-500 rounded-full w-3.5 h-3.5 transition-all duration-300 group-hover:bg-indigo-500 group-hover:scale-110 shadow-sm" />
      <div className="flex flex-col gap-0.5">
        <h4 className="font-semibold text-slate-800 text-sm md:text-base leading-snug">
          {title}
        </h4>
        <p className="text-slate-500 text-xs font-mono">
          {location} &bull; {year}
        </p>
      </div>
    </div>
  );
}

function EducationSlotSkeleton() {
  return (
    <div className="relative">
      <div className="absolute -left-7.75 top-1.5 bg-white border-2 border-slate-200 rounded-full w-3.5 h-3.5" />
      <div className="flex flex-col gap-1.5">
        <Skeleton className={cn("h-5.5 bg-gray-300 w-full md:w-60")} />
        <Skeleton className={cn("h-4 bg-gray-300 w-full md:w-60")} />
      </div>
    </div>
  );
}

async function EducationAndCaertificationsList({
  doctorId,
}: {
  doctorId: string;
}) {
  const doctorData = await getDoctorInformation(doctorId);
  return (
    <>
      {doctorData?.educationDetails.map((educ, i) => (
        <EducationSlot
          key={i}
          location={educ.institution}
          title={educ.degree}
          year={educ.year}
        />
      ))}
      {doctorData?.certifications.map((cert, i) => (
        <EducationSlot
          key={i}
          location={cert.issuingBody}
          title={cert.name}
          year={cert.year}
        />
      ))}
    </>
  );
}
