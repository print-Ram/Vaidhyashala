"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
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
  const router = useRouter();
  const [mode, setMode] = useState<string>("video");

  const handleCheckAvailability = () => {
    const token = localStorage.getItem("accessToken");
    const targetPath = `/book-appointment?mode=${mode}`;
    if (token) {
      router.push(targetPath);
    } else {
      router.push(`/login?redirect=${encodeURIComponent(targetPath)}`);
    }
  };

  return (
    <>
      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader>
          <CardTitle className="text-lg text-slate-800 font-semibold font-sans">
            Book Appointment
          </CardTitle>
          <CardDescription className="text-slate-500 text-xs">
            Select your preferred consultation mode to proceed
          </CardDescription>
        </CardHeader>
        <CardContent>
          <RadioGroup 
            value={mode} 
            onValueChange={setMode} 
            className="max-w-sm"
          >
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
          <PrimaryButton onClick={handleCheckAvailability}>
            Check Availability
          </PrimaryButton>
        </CardFooter>
      </Card>
    </>
  );
}

