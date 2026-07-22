import InfoAbout from "@/features/doctor-profile/components/info-about";
import InfoAppointmentCard from "@/features/doctor-profile/components/info-appointment-card";
import InfoCard from "@/features/doctor-profile/components/info-card";
import InfoEducation from "@/features/doctor-profile/components/info-education";
import InfoSpecializations from "@/features/doctor-profile/components/info-specializations";
import SlotAvailable from "@/features/doctor-profile/components/slot-available";
import { Suspense } from "react";

interface CustomerProfile {
  firstName: string;
  lastName: string;
  email: string;
}

interface Appointment {
  id: string;
  startTime: string;
  endTime: string;
  status: string;
  description: string;
  meetLink?: string;
  provider: {
    name: string;
    email: string;
  };
}

export default async function Home() {
  return (
    <>
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 py-8 md:py-12 space-y-10">
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-3 lg:gap-6 items-start">
          <div className="lg:col-span-2 space-y-4">
            <InfoCard />
            <InfoAbout />
            <InfoSpecializations />
            <InfoEducation />
          </div>

          <div className="grid grid-cols-1 gap-6 items-start md:grid-cols-2 lg:block lg:col-span-1 lg:sticky lg:top-24 lg:space-y-6">
            <SlotAvailable />
            <Suspense fallback={"Loading..."}>
              <InfoAppointmentCard />
            </Suspense>
          </div>
        </div>
      </main>
    </>
  );
}
