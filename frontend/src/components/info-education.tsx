import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";

export default function InfoEducation() {
  return (
    <>
      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader>
          <CardTitle className="text-xs font-mono text-slate-500 uppercase tracking-wider">
            Education &amp; Certifications
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="relative border-l border-slate-200 ml-2.5 pl-6 space-y-6">
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
          </div>
        </CardContent>
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
    <div className="relative group">
      {/* Circle Marker */}
      <div className="absolute -left-[31px] top-1.5 bg-white border-2 border-indigo-500 rounded-full w-3.5 h-3.5 transition-all duration-300 group-hover:bg-indigo-500 group-hover:scale-110 shadow-sm" />
      <div className="flex flex-col gap-0.5">
        <h4 className="font-semibold text-slate-800 text-sm md:text-base leading-snug">{title}</h4>
        <p className="text-slate-500 text-xs font-mono">
          {location} &bull; {year}
        </p>
      </div>
    </div>
  );
}


