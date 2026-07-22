import InfoAbout from "@/features/doctor-profile/components/info-about";
import InfoCard from "@/features/doctor-profile/components/info-card";
import InfoEducation from "@/features/doctor-profile/components/info-education";
import InfoSpecializations from "@/features/doctor-profile/components/info-specializations";

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

export default async function DoctorHomePage() {
  return (
    <>
      <main className="flex-1 max-w-4xl w-full mx-auto px-4 py-8 md:py-12 space-y-10">
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-3 lg:gap-6 items-start">
          <div className="lg:col-span-3 space-y-4">
            <InfoCard />
            <InfoAbout />
            <InfoSpecializations />
            <InfoEducation />
          </div>
        </div>
      </main>
    </>
  );
}
