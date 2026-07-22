import { Calendar, Clock, MapPin } from "lucide-react";

export default function AppointmentCard({
  type = "Appointment",
  status = "PENDING",
  doctorName = "Dr. Arshitha Vaidhya",
  date = "Fri, Jul 17, 2026",
  time = "2:00 PM",
  location = "In-Clinic Visit",
  note = "Health check up 2",
}) {
  const statusStyles = {
    PENDING: "bg-amber-50 text-amber-600 border-amber-200",
    CONFIRMED: "bg-green-50 text-green-600 border-green-200",
    CANCELLED: "bg-red-50 text-red-600 border-red-200",
    COMPLETED: "bg-gray-100 text-gray-600 border-gray-200",
  };

  return (
    <div className="w-full max-w-md bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
      {/* Top row: type tag + status badge */}
      <div className="flex items-center justify-between mb-4">
        <span className="text-xs font-medium text-indigo-600 border border-indigo-200 rounded-md px-2.5 py-1">
          {type}
        </span>
        <span
          className={`text-xs font-semibold rounded-md px-2.5 py-1 border ${
            statusStyles.PENDING
          }`}
        >
          {status}
        </span>
      </div>

      {/* Doctor name */}
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{doctorName}</h3>

      {/* Details */}
      <div className="space-y-2.5 mb-4">
        <div className="flex items-center gap-2 text-sm text-gray-700">
          <Calendar className="w-4 h-4 text-indigo-500" />
          <span>{date}</span>
        </div>
        <div className="flex items-center gap-2 text-sm text-gray-700">
          <Clock className="w-4 h-4 text-indigo-500" />
          <span>{time}</span>
        </div>
        <div className="flex items-center gap-2 text-sm text-gray-700">
          <MapPin className="w-4 h-4 text-green-600" />
          <span>{location}</span>
        </div>
      </div>

      {/* Note */}
      {note && (
        <div className="bg-gray-50 rounded-lg px-4 py-3">
          <p className="text-sm text-gray-500 italic">"{note}"</p>
        </div>
      )}
    </div>
  );
}
