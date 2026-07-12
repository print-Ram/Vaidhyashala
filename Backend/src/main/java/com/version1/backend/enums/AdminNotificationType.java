package com.version1.backend.enums;

public enum AdminNotificationType {
    DOCTOR_REGISTRATION,    // A new doctor registered and needs approval
    DOCTOR_OPT_OUT,         // A doctor has opted out of the scheme
    DOCTOR_APPROVAL_NEEDED  // Generic re-approval trigger
}
