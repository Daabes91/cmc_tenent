package com.clinic.modules.core.patient;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalPatientEntity Google OAuth functionality
 */
class GlobalPatientEntityTest {

    @Test
    void testLocalAuthenticationConstructor() {
        // Arrange
        String email = "test@example.com";
        String phone = "1234567890";
        String passwordHash = "hashed_password";
        LocalDate dob = LocalDate.of(1990, 1, 1);

        // Act
        GlobalPatientEntity patient = new GlobalPatientEntity(email, phone, passwordHash, dob);

        // Assert
        assertNotNull(patient.getExternalId());
        assertTrue(patient.getExternalId().startsWith("PAT-"));
        assertEquals(email, patient.getEmail());
        assertEquals(phone, patient.getPhone());
        assertEquals(passwordHash, patient.getPasswordHash());
        assertEquals(dob, patient.getDateOfBirth());
        assertEquals(AuthProvider.LOCAL, patient.getAuthProvider());
        assertNull(patient.getGoogleId());
        assertNull(patient.getGoogleEmail());
        assertTrue(patient.hasLocalAuth());
        assertFalse(patient.hasGoogleAuth());
    }

    @Test
    void testGoogleAuthenticationConstructor() {
        // Arrange
        String googleId = "google_123456";
        String email = "test@gmail.com";
        String firstName = "John";
        String lastName = "Doe";

        // Act
        GlobalPatientEntity patient = new GlobalPatientEntity(googleId, email, firstName, lastName);

        // Assert
        assertNotNull(patient.getExternalId());
        assertTrue(patient.getExternalId().startsWith("PAT-"));
        assertEquals(googleId, patient.getGoogleId());
        assertEquals(email, patient.getEmail());
        assertEquals(email, patient.getGoogleEmail());
        assertEquals(AuthProvider.GOOGLE, patient.getAuthProvider());
        assertNull(patient.getPasswordHash());
        assertFalse(patient.hasLocalAuth());
        assertTrue(patient.hasGoogleAuth());
    }

    @Test
    void testLinkGoogleAccount() {
        // Arrange
        GlobalPatientEntity patient = new GlobalPatientEntity(
            "test@example.com",
            "1234567890",
            "hashed_password",
            LocalDate.of(1990, 1, 1)
        );
        String googleId = "google_123456";
        String googleEmail = "test@gmail.com";

        // Act
        patient.linkGoogleAccount(googleId, googleEmail);

        // Assert
        assertEquals(googleId, patient.getGoogleId());
        assertEquals(googleEmail, patient.getGoogleEmail());
        assertEquals(AuthProvider.BOTH, patient.getAuthProvider());
        assertTrue(patient.hasLocalAuth());
        assertTrue(patient.hasGoogleAuth());
    }

    @Test
    void testLinkGoogleAccountWhenAlreadyLinked() {
        // Arrange
        GlobalPatientEntity patient = new GlobalPatientEntity(
            "google_123456",
            "test@gmail.com",
            "John",
            "Doe"
        );

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            patient.linkGoogleAccount("google_789", "another@gmail.com");
        });
    }

    @Test
    void testHasGoogleAuth() {
        // Test with Google-only patient
        GlobalPatientEntity googlePatient = new GlobalPatientEntity(
            "google_123",
            "test@gmail.com",
            "John",
            "Doe"
        );
        assertTrue(googlePatient.hasGoogleAuth());

        // Test with local-only patient
        GlobalPatientEntity localPatient = new GlobalPatientEntity(
            "test@example.com",
            "1234567890",
            "hashed_password",
            LocalDate.of(1990, 1, 1)
        );
        assertFalse(localPatient.hasGoogleAuth());
    }

    @Test
    void testHasLocalAuth() {
        // Test with local-only patient
        GlobalPatientEntity localPatient = new GlobalPatientEntity(
            "test@example.com",
            "1234567890",
            "hashed_password",
            LocalDate.of(1990, 1, 1)
        );
        assertTrue(localPatient.hasLocalAuth());

        // Test with Google-only patient
        GlobalPatientEntity googlePatient = new GlobalPatientEntity(
            "google_123",
            "test@gmail.com",
            "John",
            "Doe"
        );
        assertFalse(googlePatient.hasLocalAuth());
    }
}
