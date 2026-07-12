package com.version1.backend.enums;

public enum Role {
    CUSTOMER,
    DOCTOR,
    PROVIDER, // Clinic admin / owner — approves doctors, sees everything
    ADMIN // Super admin (reserved for future platform-level use)
}
