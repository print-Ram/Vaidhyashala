import Image from "next/image";
import { Card } from "./ui/card";

export default function InfoCard() {
  return (
    <>
      <Card className="border-slate-200 bg-white shadow-sm p-6 md:p-8">
        <section className="flex flex-col md:flex-row gap-6 md:gap-8 items-center md:items-start text-center md:text-left">
          <div className="overflow-hidden rounded-full w-[130px] h-[130px] md:w-[150px] md:h-[150px] border border-slate-100 shadow-sm shrink-0 bg-slate-50">
            <Image
              src="/doctor.png"
              alt="Doctor's Image"
              width={150}
              height={150}
              className="scale-105 object-cover w-full h-full"
            />
          </div>
          <section className="flex flex-col gap-2.5 justify-center">
            <span className="bg-indigo-50 border border-indigo-100 font-mono text-indigo-700 px-3.5 py-1 text-xs w-fit rounded-full uppercase tracking-wider mx-auto md:mx-0 font-medium">
              Endocrinology
            </span>
            <h2 className="font-sans text-2xl md:text-3xl font-bold text-slate-800">
              Dr. Arshitha Vaidhya
            </h2>
            <p className="text-slate-600 text-sm md:text-base leading-relaxed">
              Senior Consultant Endocrinologist (12+ Years Experience)
            </p>
            <p className="text-slate-400 text-xs font-mono tracking-wide">
              Medical Council Reg: <span className="text-indigo-600 font-semibold">MCI-48921</span>
            </p>
          </section>
        </section>
      </Card>
    </>
  );
}


