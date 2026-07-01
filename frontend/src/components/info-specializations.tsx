import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";

export default function InfoSpecializations() {
  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Specializations</CardTitle>
        </CardHeader>
        <CardContent>
          <ul>
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
    <>
      <li className="flex flex-col my-4 bg-(--gray-400) w-fit px-3 py-2 rounded-[10px]">
        <span>{title}</span>
      </li>
    </>
  );
}
