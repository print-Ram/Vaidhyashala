import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";

export default function InfoSpecializations() {
  return (
    <>
      <Card className="border-slate-800/80 bg-slate-900/40 shadow-md">
        <CardHeader>
          <CardTitle className="text-lg text-slate-100 font-sans">Specializations</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="flex flex-wrap gap-2.5">
            <SpecializationSlot title="Diabetes" />
            <SpecializationSlot title="Thyroid" />
            <SpecializationSlot title="PCOS" />
            <SpecializationSlot title="Osteoporosis" />
          </ul>
        </CardContent>
      </Card>
    </>
  );
}

function SpecializationSlot({ title }: { title: string }) {
  return (
    <li className="bg-indigo-50 border border-indigo-100 text-indigo-700 font-mono text-xs px-3.5 py-1.5 rounded-full hover:bg-indigo-100/60 transition-colors cursor-default font-medium">
      {title}
    </li>
  );
}

