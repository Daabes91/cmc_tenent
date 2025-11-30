package com.clinic.modules.core.patient;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PatientEntity Google OAuth functionality
 */
class PatientEntityTest {

    @Test
    void testPatientEntityCreation() {
        // Arrange & Act
        PatientEntity patient = new PatientEntity(
            "John",
            "Doe",
            "john.doe@example.com",
            "1234567890"
        );

        // Assert
        assertNotNull(patient.getExternalId());
        assertTrue(patient.getExternalId().startsWith("PAT-"));
        assertEquals("John", patient.getFirstName());
        assertEquals("Doe", patient.getLastName());
        assertEquals("john.doe@example.com", patient.getEmail());
        assertEquals("1234567890", patient.getPhone());
        assertNull(patient.getGoogleId());
        assertFalse(patient.hasGoogleAuth());
    }

    @Test
    void testLinkGoogleAccount() {
        // Arrange
        PatientEntity patient = new PatientEntity(
            "John",
            "Doe",
            "john.doe@example.com",
            "1234567890"
        );
        String googleId = "google_123456";

        // Act
        patient.linkGoogleAccount(googleId);

        // Assert
        assertEquals(googleId, patient.getGoogleId());
        assertTrue(patient.hasGoogleAuth());
    }

    @Test
    void testLinkGoogleAccountWhenAlreadyLinked() {
        // Arrange
        PatientEntity patient = new PatientEntity(
            "John",
            "Doe",
            "john.doe@example.com",
            "1234567890"
        );
        patient.linkGoogleAccount("google_123");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            patient.linkGoogleAccount("google_789");
        });
    }

    @Test
    void testHasGoogleAuth() {
        // Arrange
        PatientEntity patient = new PatientEntity(
            "John",
            "Doe",
            "john.doe@example.com",
            "1234567890"
        );

        // Assert - initially no Google auth
        assertFalse(patient.hasGoogleAuth());

        // Act - link Google account
        patient.linkGoogleAccount("google_123");

        // Assert - now has Google auth
        assertTrue(patient.hasGoogleAuth());
    }

    @Test
    void testSetGoogleIdDirectly() {
        // Arrange
        PatientEntity patient = new PatientEntity(
            "John",
            "Doe",
            "john.doe@example.com",
            "1234567890"
        );

        // Act
        patient.setGoogleId("google_123456");

        // Assert
        assertEquals("google_123456", patient.getGoogleId());
        assertTrue(patient.hasGoogleAuth());
    }
}
