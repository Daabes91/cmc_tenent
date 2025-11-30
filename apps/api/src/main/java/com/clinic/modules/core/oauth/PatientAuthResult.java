package com.clinic.modules.core.oauth;

/**
 * Result of patient authentication via Google OAuth.
 * Contains information about the authenticated patient and whether
 * this was a new patient creation or account linking event.
 */
public record PatientAuthResult(
    Long patientId,
    Long globalPatientId,
    String email,
    String firstName,
    String lastName,
    boolean isNewPatient,
    boolean accountLinked
) {}
