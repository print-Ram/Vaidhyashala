import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";

export default function InfoAbout() {
  return (
    <>
      <Card className="border-slate-800/80 bg-slate-900/40 shadow-md">
        <CardHeader>
          <CardTitle className="text-lg text-slate-100 font-sans">About Dr. Arshitha Vaidhya</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-slate-300 text-sm md:text-base leading-relaxed">
            Dr. Arshitha Vaidhya is a highly experienced Endocrinologist with
            over 12 years of dedicated practice in diagnosing and managing
            hormonal disorders. She specializes in treating conditions such as
            diabetes, thyroid disorders, PCOS, and osteoporosis, providing
            comprehensive care to patients seeking long-term wellness and
            hormonal balance.
          </p>
        </CardContent>
      </Card>
    </>
  );
}

