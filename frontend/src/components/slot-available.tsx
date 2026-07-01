import Link from "next/link";
import { Card, CardContent, CardDescription } from "./ui/card";
import { Separator } from "./ui/separator";

export default function SlotAvailable() {
  return (
    <>
      <Card className="px-5 gap-0 w-full">
        <CardDescription className="text-[1rem] pb-3.5 flex items-center justify-between">
          <span className="font-mono">NEXT AVAILABLE</span>
          <Link href="/" className="text-(--blue-700)">
            Full Schedule
          </Link>
        </CardDescription>
        <Separator />
        <CardContent className="text-[1rem] flex justify-between px-0 pt-3">
          <NumberOfSlots day="Today" slots={3} />
          <Separator orientation="vertical" />
          <NumberOfSlots day="Tomorrow" slots={3} />
          <Separator orientation="vertical" />
          <NumberOfSlots day="Wed" slots={0} />
        </CardContent>
      </Card>
    </>
  );
}

function NumberOfSlots({ day, slots }: { day: string; slots: number }) {
  return (
    <div className="flex flex-col gap-1 items-center">
      <span>{day}</span>
      <span className="text-(--green)">
        {slots > 0 ? `${slots} slots` : "Booked"}
      </span>
    </div>
  );
}
