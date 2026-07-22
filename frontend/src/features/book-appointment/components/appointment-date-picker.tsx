"use client";

import React from "react";
import { addDays, addMonths } from "date-fns";
import { CalendarIcon } from "lucide-react";
import type { FieldError as RHFFieldError } from "react-hook-form";

import { Button } from "@/components/ui/button";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";

interface AppointmentDatePickerProps {
  value?: Date;
  onChange: (date?: Date) => void;
  error?: RHFFieldError;
}

export default function AppointmentDatePicker({
  value,
  onChange,
  error,
}: AppointmentDatePickerProps) {
  const today = new Date();
  const maxDate = addDays(today, 20);
  const endMonth = addMonths(today, 2);

  const [open, setOpen] = React.useState(false);

  return (
    <Field>
      <FieldLabel className="font-mono uppercase">Select Date</FieldLabel>

      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button
            type="button"
            variant="outline"
            aria-invalid={!!error}
            className="justify-between w-full font-normal"
          >
            {value ? value.toLocaleDateString() : "Select date"}

            <CalendarIcon className="h-4 w-4" />
          </Button>
        </PopoverTrigger>

        <PopoverContent className="w-auto overflow-hidden p-0" align="start">
          <Calendar
            mode="single"
            selected={value}
            defaultMonth={value}
            onSelect={(selectedDate) => {
              onChange(selectedDate);
              setOpen(false);
            }}
            disabled={{
              before: today,
              after: maxDate,
            }}
            startMonth={today}
            endMonth={endMonth}
          />
        </PopoverContent>
      </Popover>

      {error && <FieldError errors={[error]} />}
    </Field>
  );
}
