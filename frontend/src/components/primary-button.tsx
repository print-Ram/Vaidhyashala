import { MoveRight } from "lucide-react";
import { Button } from "./ui/button";

export default function PrimaryButton({ children }: { children: string }) {
  return (
    <Button className="h-auto group w-full py-2.5">
      <>
        {children}
        <MoveRight
          size={14}
          className="h-4 w-4 transition-transform duration-100 ease-out group-hover:translate-x-1"
        />
      </>
    </Button>
  );
}
