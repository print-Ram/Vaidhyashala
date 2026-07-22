import { Separator } from "@/components/ui/separator";

export default function FormSeparator() {
  return (
    <div className="flex items-center gap-2 mt-4 mb-4">
      <Separator className="flex-1" />
      <span className="">OR CONTINUE WITH</span>
      <Separator className="flex-1" />
    </div>
  );
}
