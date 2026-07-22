import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import AppointmentFormWrapper from "@/features/book-appointment/components/appointment-form-wrapper";
import { Suspense } from "react";

export default function DatePage({
  params,
}: {
  params: Promise<{ doctorId: string }>;
}) {
  return (
    <>
      <main className="flex-1 max-w-2xl w-full mx-auto px-4 mt-8">
        <Card className="border-slate-200 bg-white shadow-xl">
          <CardHeader>
            <CardTitle className="text-2xl text-slate-800 font-semibold">
              Schedule Consultation
            </CardTitle>
            <CardDescription className="text-slate-500 text-lg">
              Confirm your booking with{" "}
              <strong className="text-black/65">Arshitha</strong>
            </CardDescription>
          </CardHeader>
          <Suspense fallback={<p>Loading...</p>}>
            <AppointmentFormWrapper params={params} />
          </Suspense>
        </Card>
      </main>
    </>
  );
}
