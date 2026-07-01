import { Card, CardDescription, CardHeader, CardTitle } from "./ui/card";

export default function InfoAbout() {
  return (
    <>
      <Card className="px-5">
        <CardHeader>
          <CardTitle>About Dr. Arshitha Vaidhya</CardTitle>
          <p>
            Dr. Arshitha Vaidhya is a highly experienced Endocrinologist with
            over 12 years of dedicated practice in diagnosing and managing
            hormonal disorders. She specializes in treating conditions such as
            diabetes, thyroid disorders, PCOS, and osteoporosis, providing
            comprehensive care to patients seeking long-term wellness and
            hormonal balance.
          </p>
        </CardHeader>
      </Card>
    </>
  );
}
