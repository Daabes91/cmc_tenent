package com.clinic.modules.saas;

import com.clinic.modules.saas.dto.TenantCreateRequest;
import com.clinic.modules.saas.dto.TenantUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test validation rules for SAAS DTOs
 */
class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidTenantCreateRequest() {
        TenantCreateRequest request = new TenantCreateRequest(
                "valid-slug-123",
                "Valid Clinic Name",
                "example.com");

        Set<ConstraintViolation<TenantCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testTenantCreateRequest_SlugTooShort() {
        TenantCreateRequest request = new TenantCreateRequest(
                "ab",
                "Valid Clinic Name",
                null);

        Set<ConstraintViolation<TenantCreateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("3 and 64 characters"));
    }

    @Test
    void testTenantCreateRequest_SlugTooLong() {
        TenantCreateRequest request = new TenantCreateRequest(
                "a".repeat(65),
                "Valid Clinic Name",
                null);

        Set<ConstraintViolation<TenantCreateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("3 and 64 characters"));
    }

    @Test
    void testTenantCreateRequest_SlugInvalidCharacters() {
        TenantCreateRequest request = new TenantCreateRequest(
                "Invalid_Slug",
                "Valid Clinic Name",
                null);

        Set<ConstraintViolation<TenantCreateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("lowercase letters, numbers, and hyphens"));
    }

    @Test
    void testTenantCreateRequest_SlugWithUppercase() {
        TenantCreateRequest request = new TenantCreateRequest(
                "Invalid-Slug",
                "Valid Clinic Name",
                null);

        Set<ConstraintViolation<TenantCreateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("lowercase letters, numbers, and hyphens"));
    }

    @Test
    void testTenantCreateRequest_BlankSlug() {
        TenantCreateRequest request = new TenantCreateRequest(
                "",
                "Valid Clinic Name",
                null);

        Set<ConstraintViolation<TenantCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testTenantCreateRequest_BlankName() {
        TenantCreateRequest request = new TenantCreateRequest(
                "valid-slug",
                "",
                null);

        Set<ConstraintViolation<TenantCreateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Name is required"));
    }

    @Test
    void testTenantCreateRequest_InvalidCustomDomain() {
        TenantCreateRequest request = new TenantCreateRequest(
                "valid-slug",
                "Valid Clinic Name",
                "invalid domain");

        Set<ConstraintViolation<TenantCreateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("valid domain name format"));
    }

    @Test
    void testTenantCreateRequest_ValidCustomDomain() {
        TenantCreateRequest request = new TenantCreateRequest(
                "valid-slug",
                "Valid Clinic Name",
                "subdomain.example.com");

        Set<ConstraintViolation<TenantCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid domain should have no violations");
    }

    @Test
    void testTenantUpdateRequest_ValidUpdate() {
        TenantUpdateRequest request = new TenantUpdateRequest(
                "Updated Name",
                "newdomain.example.com",
                null);

        Set<ConstraintViolation<TenantUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid update request should have no violations");
    }

    @Test
    void testTenantUpdateRequest_InvalidCustomDomain() {
        TenantUpdateRequest request = new TenantUpdateRequest(
                "Updated Name",
                "not a valid domain",
                null);

        Set<ConstraintViolation<TenantUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("valid domain name format"));
    }

    @Test
    void testTenantUpdateRequest_NullFieldsAllowed() {
        TenantUpdateRequest request = new TenantUpdateRequest(null, null, null);

        Set<ConstraintViolation<TenantUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Null fields should be allowed in update request");
    }
}
