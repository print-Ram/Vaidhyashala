"use client";

import { MODES, TMode } from "@/lib/schema";
import { useRouter } from "next/navigation";
import { parseAsStringLiteral, useQueryState } from "nuqs";
import PrimaryButton from "@/components/primary-button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Field,
  FieldContent,
  FieldDescription,
  FieldLabel,
  FieldTitle,
} from "@/components/ui/field";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";

export default function InfoAppointmentCard() {
  const router = useRouter();

  const [mode, setMode] = useQueryState(
    "mode",
    parseAsStringLiteral(MODES).withDefault("video"),
  );

  const handleCheckAvailability = () => {
    router.push(
      `/1f13bfe1-71fc-42fd-b646-8732b396f04d/book-appointment/?mode=${mode}`,
    );
  };

  const handleModeChange = (value: TMode) => {
    setMode(value);
  };

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle className="text-lg text-slate-800 font-semibold font-sans">
            Book Appointment
          </CardTitle>
          <CardDescription className="text-slate-500 text-xs">
            Select your preferred consultation mode to proceed
          </CardDescription>
        </CardHeader>
        <CardContent>
          <RadioGroup value={mode} onValueChange={handleModeChange}>
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
          <PrimaryButton
            onClick={() => {
              handleCheckAvailability();
            }}
          >
            Check Availability
          </PrimaryButton>
        </CardFooter>
      </Card>
    </>
  );
}
