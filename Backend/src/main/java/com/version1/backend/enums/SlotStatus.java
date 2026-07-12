package com.version1.backend.enums;

public enum SlotStatus {
    NORMAL,         // Regular availability
    IN_DEMAND,      // Moderately popular
    FILLING_FAST,   // Slots running out
    HIGH_DEMAND,    // Very popular, few left
    ALMOST_FULL     // Only 1–2 slots remaining
}
