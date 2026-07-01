import { Card, CardContent, CardDescription } from "./ui/card";
import { Separator } from "./ui/separator";

export default function InfoEducation() {
  return (
    <>
      <Card className="px-10">
        <CardDescription className="font-mono">
          EDUCATION &amp; CERTIFICATIONS
        </CardDescription>
        <Separator />
        <ul>
          <EducationSlot
            title="Fellowship in Metabolic disorders"
            location="Mayo Clinic"
            year={2018}
          />
          <EducationSlot
            title="MD in Endocrinology"
            location="Kasturba Medical College"
            year={2015}
          />
          <EducationSlot
            title="MBBS"
            location="Kasturba Medical College"
            year={2010}
          />
        </ul>
      </Card>
    </>
  );
}

function EducationSlot({
  title,
  location,
  year,
}: {
  title: string;
  location: string;
  year: number;
}) {
  return (
    <>
      <li className="flex flex-col mb-4">
        <span>{title}</span>
        <span className="text-(--gray-700)">
          {location} - {year}
        </span>
      </li>
    </>
  );
}
