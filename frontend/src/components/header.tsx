import { CUSTOMER_ENDPOINT, DOCTOR_PROFILE_ENDPOINT } from "@/lib/secrets";
import { cookies } from "next/headers";
import { Suspense } from "react";
import Logo from "./logo";
import AuthHeaderSkeleton from "./skeletons/auth-header-skeleton";
import UserMenu from "./user-menu";

export default async function Header({
  user,
}: {
  user: "USER" | "DOCTOR" | "ADMIN";
}) {
  return (
    <header className="border-b border-slate-200/80 bg-white/80 backdrop-blur-md sticky top-0 z-50 shadow-sm">
      <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
        <Logo className="text-xl hover:opacity-90 transition-opacity md:text-2xl" />

        {user === "USER" ? (
          <UserHeader user={user} />
        ) : (
          <DoctorHeader user={user} />
        )}
      </div>
    </header>
  );
}

async function UserName({ user }: { user: "USER" | "DOCTOR" | "ADMIN" }) {
  const fetchEndPoint =
    user === "USER" ? CUSTOMER_ENDPOINT : DOCTOR_PROFILE_ENDPOINT;
  const cookieStore = await cookies();
  const token = cookieStore.get("accessToken")?.value;

  let data;
  if (token) {
    try {
      const resp = await fetch(fetchEndPoint, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!resp.ok) {
      }

      data = await resp.json();
      console.log(data);
    } catch (err) {
      console.error("Something went wrong while logging");
    }
  }

  return <UserMenu user={user} data={data} />;
}

function UserHeader({ user }: { user: "USER" | "DOCTOR" | "ADMIN" }) {
  return (
    <>
      <nav className="flex items-center gap-4">
        <div className="flex items-center gap-6">
          <Suspense fallback={<AuthHeaderSkeleton />}>
            <UserName user={user} />
          </Suspense>
        </div>
      </nav>
    </>
  );
}

function DoctorHeader({ user }: { user: "USER" | "DOCTOR" | "ADMIN" }) {
  return (
    <>
      <nav className="flex items-center gap-4">
        <div className="flex items-center gap-6">
          <Suspense fallback={<AuthHeaderSkeleton />}>
            <UserName user={user} />
          </Suspense>
        </div>
      </nav>
    </>
  );
}
