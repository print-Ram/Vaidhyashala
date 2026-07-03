import InfoAbout from "@/components/info-about";
import InfoAppointmentCard from "@/components/info-appointment-card";
import InfoCard from "@/components/info-card";
import InfoEducation from "@/components/info-education";
import InfoHeader from "@/components/info-header";
import InfoSpecializations from "@/components/info-specializations";
import SlotAvailable from "@/components/slot-available";

export default function Home() {
  return (
    <>
      <InfoHeader />
      <SlotAvailable />
      <InfoAppointmentCard />
      <InfoCard />
      <InfoAbout />
      <InfoEducation />
      <InfoSpecializations />
    </>
  );
}
