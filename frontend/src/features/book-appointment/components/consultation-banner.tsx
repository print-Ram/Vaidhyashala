import { Hospital, Video } from "lucide-react";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { TMode } from "@/lib/schema";

export default function ConsultationBanner({ mode }: { mode: TMode }) {
  return (
    <>
      <Card className="bg-gray-100">
        <CardHeader>
          <div className="flex flex-col gap-y-2 md:flex-row justify-between">
            <div>
              <CardTitle>
                <strong>Dr. Arshitha</strong>
              </CardTitle>
              <span className="">Endocrinology Specialist</span>
            </div>
            <div className="flex items-center gap-2 py-2 w-fit px-4 rounded-2xl bg-gray-200">
              {mode === "clinic" ? (
                <>
                  <Hospital size={"22px"} /> Clinic (30m)
                </>
              ) : (
                <>
                  <Video size={"22px"} /> Video (20m)
                </>
              )}
            </div>
          </div>
        </CardHeader>
      </Card>
    </>
  );
}
