package com.clinic.modules.admin.staff.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for resending an invitation email to a staff member.
 */
public record ResendInvitationRequest(
        @NotNull(message = "Staff ID is required")
        Long staffId
) {
}
