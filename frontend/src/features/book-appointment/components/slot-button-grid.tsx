import { Clock } from "lucide-react";
import type { FieldError as RHFFieldError } from "react-hook-form";

import SlotButton from "./slot-button";
import { Field, FieldLabel, FieldError } from "@/components/ui/field";

interface SlotButtonGridProps {
  value: string;
  onChange: (value: string) => void;
  error?: RHFFieldError;
}

const timeSlots = [
  { label: "09:00 AM", value: "09:00:00" },
  { label: "10:00 AM", value: "10:00:00" },
  { label: "11:00 AM", value: "11:00:00" },
  { label: "12:00 PM", value: "12:00:00" },
  { label: "02:00 PM", value: "14:00:00" },
  { label: "03:00 PM", value: "15:00:00" },
  { label: "04:00 PM", value: "16:00:00" },
  { label: "05:00 PM", value: "17:00:00" },
];

export default function SlotButtonGrid({
  value,
  onChange,
  error,
}: SlotButtonGridProps) {
  return (
    <Field data-invalid={!!error}>
      <FieldLabel className="font-mono uppercase tracking-wider text-xs text-slate-500">
        Select Available Slot
      </FieldLabel>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
        {timeSlots.map((slot) => (
          <SlotButton
            key={slot.value}
            type="button"
            disabled={false}
            isSelected={value === slot.value}
            aria-invalid={!!error}
            onClick={() => onChange(slot.value)}
          >
            <Clock className="w-4 h-4 opacity-60" />
            {slot.label}
          </SlotButton>
        ))}
      </div>

      {error && <FieldError errors={[error]} />}
    </Field>
  );
}
