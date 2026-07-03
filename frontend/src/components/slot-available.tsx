import Link from "next/link";
import { Card, CardContent, CardDescription } from "./ui/card";
import { Separator } from "./ui/separator";

export default function SlotAvailable() {
  return (
    <>
      <Card className="border-slate-200 bg-white shadow-sm px-5 py-4 w-full flex flex-col gap-3.5">
        <CardDescription className="text-sm font-mono flex items-center justify-between pb-1 text-slate-400">
          <span>NEXT AVAILABLE</span>
          <Link href="/" className="text-indigo-600 hover:text-indigo-700 font-sans font-medium text-xs">
            Full Schedule
          </Link>
        </CardDescription>
        <Separator className="bg-slate-100" />
        <CardContent className="text-sm flex justify-between px-0 py-0 text-slate-600">
          <NumberOfSlots day="Today" slots={3} />
          <Separator orientation="vertical" className="bg-slate-100 h-10" />
          <NumberOfSlots day="Tomorrow" slots={3} />
          <Separator orientation="vertical" className="bg-slate-100 h-10" />
          <NumberOfSlots day="Wed" slots={0} />
        </CardContent>
      </Card>
    </>
  );
}

function NumberOfSlots({ day, slots }: { day: string; slots: number }) {
  return (
    <div className="flex flex-col gap-1 items-center font-sans">
      <span className="text-xs text-slate-500 font-medium">{day}</span>
      <span className={slots > 0 ? "text-emerald-600 font-semibold text-sm" : "text-slate-400 text-sm"}>
        {slots > 0 ? `${slots} slots` : "Booked"}
      </span>
    </div>
  );
}

