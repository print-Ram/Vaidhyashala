import { DoctorSchema } from "@/lib/schema";
import { DOCTOR_ENDPOINT } from "@/lib/secrets";

export async function getDoctorInformation(doctorId: string) {
  const DOCTOR_ID_ENDPOINT = DOCTOR_ENDPOINT(doctorId);
  try {
    const resp = await fetch(DOCTOR_ID_ENDPOINT);
    if (!resp.ok) {
      throw new Error("Failed to fetch doctor's data");
    }
    console.log(resp);
    const respJson = await resp.json();
    console.log(respJson);
    const result = DoctorSchema.safeParse(respJson);
    if (!result.success) {
      throw new Error(
        `Doctor data for ${doctorId} did not match expected shape`,
      );
    }
    return result.data;
  } catch (err) {
    return null;
  }
}
