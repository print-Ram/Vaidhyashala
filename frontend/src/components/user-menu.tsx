"use client";

import { useState } from "react";
import Link from "next/link";
import { Drawer, DrawerContent, DrawerTrigger } from "@/components/ui/drawer";
import { Button } from "@/components/ui/button";
import { LogIn, LogOut, Menu, User } from "lucide-react";
import LogoutButton from "./logout-button";

export default function UserMenu({
  user,
  data,
}: {
  user: "USER" | "DOCTOR" | "ADMIN";
  data: any;
}) {
  const [open, setOpen] = useState(false);

  if (!data) {
    return (
      <Link
        className="flex items-center gap-1.5 text-xs font-mono text-indigo-600 hover:text-indigo-700 hover:bg-indigo-50 border border-indigo-200 px-4 py-2 rounded-lg transition-all"
        href={user === "USER" ? "/login" : "/doctor/login"}
      >
        Sign In
      </Link>
    );
  }

  return (
    <>
      {/* Desktop */}
      <div className="hidden md:flex items-center gap-4">
        <span className="font-mono flex items-center gap-1.5 text-sm">
          <User className="h-4 w-4 text-indigo-600" />
          {data.firstName}
        </span>

        {user === "DOCTOR" && (
          <Link href="/doctor/appointments">My Appointments</Link>
        )}

        <LogoutButton>
          <LogOut />
          SIGN OUT
        </LogoutButton>
      </div>

      {/* Mobile */}
      <div className="md:hidden">
        <Drawer open={open} onOpenChange={setOpen}>
          <DrawerTrigger asChild>
            <Button size="icon" variant="ghost">
              <Menu />
            </Button>
          </DrawerTrigger>

          <DrawerContent>
            <div className="p-6 flex flex-col gap-4">
              <div className="flex items-center gap-2">
                <User className="h-4 w-4" />
                {data.firstName}
              </div>

              {user === "DOCTOR" && (
                <Link href="/doctor/appointments">My Appointments</Link>
              )}

              <LogoutButton>
                <LogOut />
                SIGN OUT
              </LogoutButton>
            </div>
          </DrawerContent>
        </Drawer>
      </div>
    </>
  );
}
