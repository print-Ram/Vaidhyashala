package com.version1.backend.enums;

public enum DoctorStatus {
    PENDING_APPROVAL,   // Newly registered, awaiting PROVIDER approval
    ACTIVE,             // Approved and available for appointments
    OPTED_OUT,          // Doctor voluntarily opted out (admin notified)
    SUSPENDED           // Suspended by admin
}
