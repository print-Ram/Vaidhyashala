import { CardTitle } from "@/components/ui/card";

export default function FormHeaderTitle({ children }: { children: string }) {
  return (
    <CardTitle className="text-[clamp(1.3rem,4vw,1.5rem)] font-sans font-semibold mx-auto mb-3">
      {children}
    </CardTitle>
  );
}
