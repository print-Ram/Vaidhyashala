import Image from "next/image";
import { Card } from "./ui/card";

export default function InfoCard() {
  return (
    <>
      <Card className="px-10">
        <section className="flex gap-8 items-center">
          <div className="overflow-hidden rounded-full w-[150px] h-[150px]">
            <Image
              src="/doctor.png"
              alt="Doctor's Image"
              width={150}
              height={150}
              className="scale-105 object-cover"
            />
          </div>
          <section className="flex flex-col gap-2">
            <span className="bg-(--gray-400) font-mono px-3 py-1 w-fit rounded-[5px]">
              Endocrinology
            </span>
            <span className="font-sans text-headline-md">
              Dr. Arshitha Vaidhya
            </span>
            <span>
              Senior Consultant Endocrinologist (12+ Years Experience)
            </span>
            <span>
              Medical Council Reg: <span className="font-mono">MCI-48921</span>
            </span>
          </section>
        </section>
      </Card>
    </>
  );
}
