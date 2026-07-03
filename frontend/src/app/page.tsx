"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import InfoAbout from "@/components/info-about";
import InfoAppointmentCard from "@/components/info-appointment-card";
import InfoCard from "@/components/info-card";
import InfoEducation from "@/components/info-education";
import InfoSpecializations from "@/components/info-specializations";
import SlotAvailable from "@/components/slot-available";
import Logo from "@/components/logo";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { LogOut, Calendar, Clock, Video, MapPin, User, LogIn, ExternalLink } from "lucide-react";
import { CUSTOMER_ENDPOINT, MY_APPOINTMENT_ENDPOINT } from "@/lib/secrets";

interface CustomerProfile {
  firstName: string;
  lastName: string;
  email: string;
}

interface Appointment {
  id: string;
  startTime: string;
  endTime: string;
  status: string;
  description: string;
  meetLink?: string;
  provider: {
    name: string;
    email: string;
  };
}

export default function Home() {
  const router = useRouter();
  const [token, setToken] = useState<string | null>(null);
  const [userProfile, setUserProfile] = useState<CustomerProfile | null>(null);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loadingAppointments, setLoadingAppointments] = useState<boolean>(false);

  // Check auth and load profile on mount
  useEffect(() => {
    const savedToken = localStorage.getItem("accessToken");
    setToken(savedToken);

    if (savedToken) {
      // Fetch user profile
      fetch(CUSTOMER_ENDPOINT, {
        headers: {
          "Authorization": `Bearer ${savedToken}`,
        },
      })
        .then((res) => {
          if (res.ok) return res.json();
          throw new Error("Failed to fetch profile");
        })
        .then((data) => setUserProfile(data))
        .catch(() => {
          // Token is likely expired, clear it
          handleSignOut();
        });

      // Fetch appointments
      setLoadingAppointments(true);
      fetch(MY_APPOINTMENT_ENDPOINT, {
        headers: {
          "Authorization": `Bearer ${savedToken}`,
        },
      })
        .then((res) => {
          if (res.ok) return res.json();
          return [];
        })
        .then((data) => setAppointments(data))
        .catch((err) => console.error("Error loading appointments:", err))
        .finally(() => setLoadingAppointments(false));
    }
  }, []);

  const handleSignOut = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    setToken(null);
    setUserProfile(null);
    setAppointments([]);
    router.refresh();
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 flex flex-col font-sans">
      {/* Top Header Navigation */}
      <header className="border-b border-slate-200/80 bg-white/80 backdrop-blur-md sticky top-0 z-50 shadow-sm">
        <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
          <Logo className="text-2xl hover:opacity-90 transition-opacity" />
          
          <nav className="flex items-center gap-4">
            {token && userProfile ? (
              <div className="flex items-center gap-3">
                <span className="text-sm text-slate-600 hidden sm:inline-flex items-center gap-1.5 font-mono">
                  <User className="w-4 h-4 text-indigo-600" />
                  {userProfile.firstName} {userProfile.lastName}
                </span>
                <button
                  onClick={handleSignOut}
                  className="flex items-center gap-1.5 text-xs font-mono text-rose-600 hover:text-rose-700 hover:bg-rose-50 border border-rose-200 px-3 py-1.5 rounded-lg transition-all cursor-pointer"
                >
                  <LogOut className="w-3.5 h-3.5" /> SIGN OUT
                </button>
              </div>
            ) : (
              <Link
                href="/login"
                className="flex items-center gap-1.5 text-xs font-mono text-indigo-600 hover:text-indigo-700 hover:bg-indigo-50 border border-indigo-200 px-4 py-2 rounded-lg transition-all"
              >
                <LogIn className="w-3.5 h-3.5" /> SIGN IN
              </Link>
            )}
          </nav>
        </div>
      </header>

      {/* Main Responsive Grid Layout */}
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 py-8 md:py-12 space-y-10">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
          {/* Left Main Content (2/3 width) */}
          <div className="lg:col-span-2 space-y-8">
            <InfoCard />
            <InfoAbout />
            <InfoSpecializations />
            <InfoEducation />
          </div>

          {/* Right Sidebar Booking Widget (1/3 width, sticky on desktop) */}
          <div className="lg:col-span-1 lg:sticky lg:top-24 space-y-6">
            <SlotAvailable />
            <InfoAppointmentCard />
          </div>
        </div>

        {/* User Appointments List Section */}
        {token && (
          <section className="border-t border-slate-200 pt-10">
            <h3 className="text-xl font-semibold text-slate-800 flex items-center gap-2 mb-6">
              <Calendar className="w-5 h-5 text-indigo-600" /> Your Scheduled Consultations
            </h3>

            {loadingAppointments ? (
              <div className="py-8 flex justify-center">
                <div className="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
              </div>
            ) : appointments.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {appointments.map((appt) => {
                  const isVideo = appt.meetLink !== null && appt.meetLink !== undefined;
                  const apptDate = new Date(appt.startTime);
                  
                  return (
                    <Card key={appt.id} className="border-slate-200 bg-white hover:border-slate-300 transition-all font-sans shadow-sm">
                      <CardHeader className="pb-3">
                        <div className="flex justify-between items-start">
                          <div>
                            <span className="text-xs font-mono bg-indigo-55 border border-indigo-100 text-indigo-700 px-2.5 py-0.5 rounded">
                              Appointment
                            </span>
                            <h4 className="text-base font-semibold text-slate-850 mt-2">Dr. Arshitha Vaidhya</h4>
                          </div>
                          <span className={`text-xs font-mono px-2.5 py-0.5 rounded uppercase border font-medium ${
                            appt.status === "CONFIRMED" 
                              ? "bg-emerald-50 border-emerald-200 text-emerald-700" 
                              : "bg-amber-50 border-amber-200 text-amber-700"
                          }`}>
                            {appt.status}
                          </span>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-3.5 pb-4">
                        <div className="flex items-center gap-2 text-slate-600 text-sm">
                          <Calendar className="w-4 h-4 text-indigo-600 shrink-0" />
                          <span>
                            {apptDate.toLocaleDateString("en-US", {
                              weekday: "short",
                              month: "short",
                              day: "numeric",
                              year: "numeric"
                            })}
                          </span>
                        </div>
                        <div className="flex items-center gap-2 text-slate-600 text-sm">
                          <Clock className="w-4 h-4 text-indigo-600 shrink-0" />
                          <span>
                            {apptDate.toLocaleTimeString("en-US", {
                              hour: "numeric",
                              minute: "2-digit"
                            })}
                          </span>
                        </div>
                        <div className="flex items-center gap-2 text-slate-600 text-sm">
                          {isVideo ? (
                            <Video className="w-4 h-4 text-indigo-600 shrink-0" />
                          ) : (
                            <MapPin className="w-4 h-4 text-emerald-600 shrink-0" />
                          )}
                          <span className="capitalize">{isVideo ? "Video Consultation" : "In-Clinic Visit"}</span>
                        </div>
                        
                        {appt.description && (
                          <p className="text-xs text-slate-500 italic bg-slate-50 p-2.5 rounded border border-slate-100">
                            &ldquo;{appt.description}&rdquo;
                          </p>
                        )}
                        
                        {isVideo && appt.meetLink && appt.status === "CONFIRMED" && (
                          <a
                            href={appt.meetLink}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="mt-3 flex items-center justify-center gap-1.5 py-2 px-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded text-xs font-medium tracking-wide transition-all shadow active:scale-[0.98] cursor-pointer"
                          >
                            Join Telehealth Video Call <ExternalLink className="w-3.5 h-3.5" />
                          </a>
                        )}
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            ) : (
              <Card className="border-dashed border-slate-300 bg-white text-center p-8 shadow-sm">
                <CardDescription className="text-slate-500 font-sans text-sm">
                  You do not have any consultations booked yet. Select a mode above to get started.
                </CardDescription>
              </Card>
            )}
          </section>
        )}
      </main>
    </div>
  );
}

