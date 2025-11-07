package com.clinic.modules.core.treatment;

public enum TreatmentPlanStatus {
    PLANNED,      // Plan created but not yet started
    IN_PROGRESS,  // At least one visit completed
    COMPLETED,    // All visits completed and paid
    CANCELLED     // Plan cancelled
}
