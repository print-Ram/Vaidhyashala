import { CardContent } from "@/components/ui/card";
import { cookies } from "next/headers";
import AppointmentForm from "./appointment-form";
import { getDoctorInformation } from "@/features/doctor-profile/get-doctor-queries";

export default async function AppointmentFormWrapper({
  params,
}: {
  params: Promise<{ doctorId: string }>;
}) {
  const token = (await cookies()).get("accessToken")?.value;
  const { doctorId } = await params;
  const doctorData = await getDoctorInformation(doctorId);
  if (!doctorData) {
    return <>Something went wrong</>;
  }
  console.log(doctorData);
  return (
    <CardContent>
      <AppointmentForm token={token} providerId={doctorData.userId} />
    </CardContent>
  );
}
