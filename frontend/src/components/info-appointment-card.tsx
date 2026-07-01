import PrimaryButton from "./primary-button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "./ui/card";
import {
  Field,
  FieldContent,
  FieldDescription,
  FieldLabel,
  FieldTitle,
} from "./ui/field";
import { RadioGroup, RadioGroupItem } from "./ui/radio-group";

export default function InfoAppointmentCard() {
  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle className="text-[1.3rem] font-sans">
            Book Appointment
          </CardTitle>
          <CardDescription>
            Select your preferred consultation mode to proceed
          </CardDescription>
        </CardHeader>
        <CardContent>
          <RadioGroup defaultValue="video" className="max-w-sm">
            <FieldLabel htmlFor="video">
              <Field orientation="horizontal">
                <FieldContent>
                  <FieldTitle>Video Consultation</FieldTitle>
                  <FieldDescription>20 mins</FieldDescription>
                </FieldContent>
                <RadioGroupItem value="video" id="video" />
              </Field>
            </FieldLabel>
            <FieldLabel htmlFor="clinic">
              <Field orientation="horizontal">
                <FieldContent>
                  <FieldTitle>In-Clinic Visit</FieldTitle>
                  <FieldDescription>30 mins</FieldDescription>
                </FieldContent>
                <RadioGroupItem value="clinic" id="clinic" />
              </Field>
            </FieldLabel>
          </RadioGroup>
        </CardContent>
        <CardFooter>
          <PrimaryButton>Check Availability</PrimaryButton>
        </CardFooter>
      </Card>
    </>
  );
}
