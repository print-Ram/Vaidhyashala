"use client";

import React, { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Logo from "@/components/logo";
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import PrimaryButton from "@/components/primary-button";
import { Calendar as CalendarIcon, Clock, Video, Home as HomeIcon, CheckCircle2, AlertTriangle, ArrowLeft } from "lucide-react";
import { PROVIDERS_APPOINTMENT_ENDPOINT, APPOINTMENT_ENDPOINT } from "@/lib/secrets";

interface Provider {
  id: string;
  name: string;
  email: string;
}

function BookingForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const mode = searchParams.get("mode") || "video"; // "video" or "clinic"

  // Form State
  const [providers, setProviders] = useState<Provider[]>([]);
  const [selectedProvider, setSelectedProvider] = useState<string>("8c30d954-dd77-4af9-8056-c8e1fa0e0eb5"); // Seeded provider by default
  const [selectedDate, setSelectedDate] = useState<string>("");
  const [selectedTime, setSelectedTime] = useState<string>("");
  const [description, setDescription] = useState<string>("");
  
  // App States
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [successData, setSuccessData] = useState<any>(null);

  // Auth check and load providers
  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      router.push(`/login?redirect=${encodeURIComponent(`/book-appointment?mode=${mode}`)}`);
      return;
    }

    const fetchProviders = async () => {
      try {
        const response = await fetch(PROVIDERS_APPOINTMENT_ENDPOINT, {
          headers: {
            "Authorization": `Bearer ${token}`,
          },
        });
        
        if (response.ok) {
          const data = await response.json();
          setProviders(data);
          if (data.length > 0) {
            // Find seeded provider or default to first
            const seeded = data.find((p: Provider) => p.email === "provider@vaidhyashala.com" || p.id === "8c30d954-dd77-4af9-8056-c8e1fa0e0eb5");
            if (seeded) {
              setSelectedProvider(seeded.id);
            } else {
              setSelectedProvider(data[0].id);
            }
          }
        } else {
          // If token is invalid/expired, redirect to login
          localStorage.removeItem("accessToken");
          localStorage.removeItem("refreshToken");
          router.push(`/login?redirect=${encodeURIComponent(`/book-appointment?mode=${mode}`)}`);
        }
      } catch (err) {
        console.error("Failed to load providers:", err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchProviders();
    
    // Set default date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    setSelectedDate(tomorrow.toISOString().split("T")[0]);
  }, [router, mode]);

  // Standard available slots
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedDate || !selectedTime) {
      setError("Please select both a date and a time slot.");
      return;
    }

    setIsSubmitting(true);
    setError(null);

    const token = localStorage.getItem("accessToken");
    const startTime = `${selectedDate}T${selectedTime}`;
    
    // Calculate end time (Video = 20 mins, Clinic = 30 mins)
    const [hrs, mins] = selectedTime.split(":").map(Number);
    let endMins = mins + (mode === "video" ? 20 : 30);
    let endHrs = hrs;
    if (endMins >= 60) {
      endMins -= 60;
      endHrs += 1;
    }
    const endHrsStr = String(endHrs).padStart(2, "0");
    const endMinsStr = String(endMins).padStart(2, "0");
    const endTime = `${selectedDate}T${endHrsStr}:${endMinsStr}:00`;

    const payload = {
      providerId: selectedProvider,
      startTime,
      endTime,
      description: description || `Consultation via ${mode === "video" ? "Video Call" : "In-Clinic Visit"}`,
    };

    try {
      const response = await fetch(APPOINTMENT_ENDPOINT, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || "Failed to book appointment. This slot might already be booked.");
      }

      setSuccessData(result);
    } catch (err: any) {
      setError(err.message || "An unexpected error occurred during booking.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const getProviderName = (id: string) => {
    if (id === "8c30d954-dd77-4af9-8056-c8e1fa0e0eb5") return "Dr. Arshitha Vaidhya";
    const found = providers.find(p => p.id === id);
    return found ? found.name : "Dr. Arshitha Vaidhya";
  };

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-slate-50 text-slate-800 p-4">
        <div className="w-12 h-12 border-4 border-indigo-650 border-t-transparent rounded-full animate-spin"></div>
        <p className="mt-4 text-slate-500 font-mono text-sm">Verifying authorization and loading slots...</p>
      </div>
    );
  }

  // Success view
  if (successData) {
    return (
      <div className="min-h-screen bg-slate-50 text-slate-850 flex flex-col items-center justify-center p-4">
        <Card className="w-full max-w-lg border-slate-200 bg-white shadow-xl animate-fade-in py-6">
          <CardHeader className="text-center">
            <div className="flex justify-center mb-4">
              <CheckCircle2 className="w-16 h-16 text-emerald-500 animate-bounce" />
            </div>
            <CardTitle className="text-2xl text-emerald-600 font-semibold">Appointment Confirmed!</CardTitle>
            <CardDescription className="text-slate-500">
              Your appointment has been successfully scheduled and synced with Google Calendar.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="bg-slate-50 border border-slate-100 rounded-lg p-5 space-y-4 font-sans">
              <div className="flex justify-between border-b border-slate-100 pb-2">
                <span className="text-slate-500 text-sm">Doctor:</span>
                <span className="font-semibold text-slate-800">{getProviderName(selectedProvider)}</span>
              </div>
              <div className="flex justify-between border-b border-slate-100 pb-2">
                <span className="text-slate-500 text-sm">Date:</span>
                <span className="font-semibold text-slate-800">
                  {new Date(successData.startTime).toLocaleDateString("en-US", {
                    weekday: "long",
                    year: "numeric",
                    month: "long",
                    day: "numeric",
                  })}
                </span>
              </div>
              <div className="flex justify-between border-b border-slate-100 pb-2">
                <span className="text-slate-500 text-sm">Time:</span>
                <span className="font-semibold text-slate-800">
                  {new Date(successData.startTime).toLocaleTimeString("en-US", {
                    hour: "numeric",
                    minute: "2-digit",
                  })}
                </span>
              </div>
              <div className="flex justify-between border-b border-slate-100 pb-2">
                <span className="text-slate-500 text-sm">Mode:</span>
                <span className="font-semibold text-slate-800 capitalize flex items-center gap-1.5">
                  {mode === "video" ? <Video className="w-4 h-4 text-indigo-600" /> : <HomeIcon className="w-4 h-4 text-emerald-500" />}
                  {mode === "video" ? "Video Consultation" : "In-Clinic Visit"}
                </span>
              </div>
              {successData.meetLink && (
                <div className="flex flex-col gap-2 pt-2">
                  <span className="text-slate-500 text-sm">Telehealth Link:</span>
                  <a
                    href={successData.meetLink}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center justify-center gap-2 py-3 px-4 bg-indigo-600 hover:bg-indigo-700 active:scale-[0.98] transition-all rounded-lg font-medium text-white shadow-md text-center cursor-pointer"
                  >
                    <Video className="w-5 h-5" />
                    Join Google Meet Call
                  </a>
                </div>
              )}
            </div>
          </CardContent>
          <CardFooter className="flex flex-col gap-3">
            <PrimaryButton className="w-full font-mono tracking-wider cursor-pointer" onClick={() => router.push("/")}>
              Back to Doctor Profile
            </PrimaryButton>
          </CardFooter>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 flex flex-col pb-12">
      {/* Mini Header */}
      <header className="border-b border-slate-200 bg-white/80 backdrop-blur-md sticky top-0 z-50 shadow-sm">
        <div className="max-w-4xl mx-auto px-4 py-4 flex justify-between items-center">
          <Logo className="text-xl" />
          <button 
            onClick={() => router.push("/")}
            className="flex items-center gap-1.5 text-xs font-mono text-slate-600 hover:text-slate-800 transition-colors bg-white border border-slate-200 px-3 py-1.5 rounded-lg cursor-pointer"
          >
            <ArrowLeft className="w-3.5 h-3.5" /> BACK TO PROFILE
          </button>
        </div>
      </header>

      {/* Main Container */}
      <main className="flex-1 max-w-2xl w-full mx-auto px-4 mt-8">
        <Card className="border-slate-200 bg-white shadow-xl">
          <CardHeader>
            <CardTitle className="text-2xl text-slate-800 font-semibold">Schedule Consultation</CardTitle>
            <CardDescription className="text-slate-500">
              Confirm your booking with {getProviderName(selectedProvider)}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {error && (
              <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-750 text-sm flex gap-2.5 items-start">
                <AlertTriangle className="w-5 h-5 text-red-500 shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Specialization & Doctor Card */}
              <div className="bg-slate-50 border border-slate-100 rounded-xl p-4 flex items-center justify-between animate-fade-in">
                <div>
                  <h4 className="text-slate-800 font-medium">{getProviderName(selectedProvider)}</h4>
                  <p className="text-slate-500 text-xs mt-0.5">Endocrinology Specialist</p>
                </div>
                <div className="text-right">
                  <span className="text-xs font-mono bg-indigo-50 border border-indigo-100 text-indigo-700 px-2.5 py-1 rounded-full capitalize inline-flex items-center gap-1.5">
                    {mode === "video" ? <Video className="w-3.5 h-3.5" /> : <HomeIcon className="w-3.5 h-3.5" />}
                    {mode === "video" ? "Video (20m)" : "In-Clinic (30m)"}
                  </span>
                </div>
              </div>

              {/* Date Selection */}
              <div className="space-y-2">
                <label className="text-xs font-mono text-slate-500 uppercase tracking-wider block">
                  Select Date
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <CalendarIcon className="h-4 w-4 text-slate-400" />
                  </div>
                  <input
                    type="date"
                    required
                    min={new Date().toISOString().split("T")[0]}
                    value={selectedDate}
                    onChange={(e) => {
                      setSelectedDate(e.target.value);
                      setSelectedTime(""); // Reset selected slot when date changes
                    }}
                    className="w-full bg-white border border-slate-200 rounded-lg py-2.5 pl-10 pr-4 text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 text-sm"
                  />
                </div>
              </div>

              {/* Time Slots Grid */}
              <div className="space-y-3">
                <label className="text-xs font-mono text-slate-500 uppercase tracking-wider block">
                  Select Available Slot
                </label>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 font-mono">
                  {timeSlots.map((slot) => {
                    const isSelected = selectedTime === slot.value;
                    return (
                      <button
                        key={slot.value}
                        type="button"
                        onClick={() => setSelectedTime(slot.value)}
                        className={`py-3 px-3 rounded-lg border text-sm font-medium transition-all text-center flex flex-col items-center justify-center gap-1 cursor-pointer ${
                          isSelected
                            ? "bg-indigo-600 border-indigo-500 text-white shadow-md shadow-indigo-100"
                            : "bg-white border-slate-200 text-slate-600 hover:border-slate-350 hover:bg-slate-50"
                        }`}
                      >
                        <Clock className="w-4 h-4 opacity-60" />
                        {slot.label}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Description Input */}
              <div className="space-y-2">
                <label className="text-xs font-mono text-slate-500 uppercase tracking-wider block">
                  Reason for Consultation (Optional)
                </label>
                <textarea
                  placeholder="Tell us briefly about your symptoms or purpose of visit..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full bg-white border border-slate-200 rounded-lg p-3 text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 text-sm h-28 resize-none"
                />
              </div>

              {/* Submit Button */}
              <PrimaryButton 
                type="submit" 
                disabled={isSubmitting || !selectedDate || !selectedTime}
                className="w-full mt-4"
              >
                {isSubmitting ? "Processing Reservation..." : "Confirm Booking"}
              </PrimaryButton>
            </form>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}

export default function BookAppointmentPage() {
  return (
    <Suspense fallback={
      <div className="flex flex-col items-center justify-center min-h-screen bg-slate-50 text-slate-800 p-4">
        <div className="w-12 h-12 border-4 border-indigo-650 border-t-transparent rounded-full animate-spin"></div>
        <p className="mt-4 text-slate-500 font-mono text-sm">Loading scheduler...</p>
      </div>
    }>
      <BookingForm />
    </Suspense>
  );
}

