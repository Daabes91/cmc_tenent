package com.clinic.modules.admin.auth.controller;

import com.clinic.modules.admin.auth.dto.AuthTokensResponse;
import com.clinic.modules.admin.auth.dto.LogoutRequest;
import com.clinic.modules.admin.auth.dto.RefreshTokenRequest;
import com.clinic.modules.admin.auth.dto.StaffLoginRequest;
import com.clinic.modules.admin.auth.dto.StaffPasswordChangeRequest;
import com.clinic.modules.admin.auth.dto.StaffProfileResponse;
import com.clinic.modules.admin.auth.dto.StaffProfileUpdateRequest;
import com.clinic.modules.admin.staff.dto.ModulePermissionsDto;
import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.auth.service.StaffAuthService;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.admin.staff.service.StaffManagementService;
import com.clinic.security.JwtPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final StaffAuthService staffAuthService;
    private final StaffUserRepository staffUserRepository;
    private final StaffManagementService staffManagementService;

    public AdminAuthController(StaffAuthService staffAuthService,
                               StaffUserRepository staffUserRepository,
                               StaffManagementService staffManagementService) {
        this.staffAuthService = staffAuthService;
        this.staffUserRepository = staffUserRepository;
        this.staffManagementService = staffManagementService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(@Valid @RequestBody StaffLoginRequest request) {
        return ResponseEntity.ok(staffAuthService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(staffAuthService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        staffAuthService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<StaffProfileResponse> profile(Authentication authentication) {
        var principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.valueOf(principal.subject());

        StaffUser staffUser = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalStateException("Staff user not found"));

        return ResponseEntity.ok(toProfileResponse(staffUser));
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<StaffProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody StaffProfileUpdateRequest request
    ) {
        var principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.valueOf(principal.subject());

        StaffUser staffUser = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalStateException("Staff user not found"));

        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedName = request.fullName().trim();

        if (!staffUser.getEmail().equalsIgnoreCase(normalizedEmail)) {
            boolean emailTaken = staffUserRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, staffId);
            if (emailTaken) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email address is already in use.");
            }
            staffUser.setEmail(normalizedEmail);
        }

        staffUser.setFullName(normalizedName);

        StaffUser saved = staffUserRepository.save(staffUser);

        return ResponseEntity.ok(toProfileResponse(saved));
    }

    private StaffProfileResponse toProfileResponse(StaffUser staffUser) {
        ModulePermissionsDto permissions = resolvePermissions(staffUser);
        return new StaffProfileResponse(
                staffUser.getId(),
                staffUser.getEmail(),
                staffUser.getFullName(),
                staffUser.getRole().name(),
                permissions
        );
    }

    @PutMapping("/profile/password")
    @Transactional
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody StaffPasswordChangeRequest request
    ) {
        var principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.valueOf(principal.subject());

        String current = request.currentPassword().trim();
        String next = request.newPassword().trim();

        if (next.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 8 characters long.");
        }

        if (current.equals(next)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password.");
        }

        staffAuthService.changePassword(staffId, current, next);
        return ResponseEntity.noContent().build();
    }

    private ModulePermissionsDto resolvePermissions(StaffUser staffUser) {
        if (staffUser.getRole() == StaffRole.ADMIN) {
            return null;
        }
        try {
            return staffManagementService.getStaffPermissions(staffUser.getId());
        } catch (IllegalArgumentException e) {
            return new ModulePermissionsDto(
                    java.util.Collections.emptySet(),
                    java.util.Collections.emptySet(),
                    java.util.Collections.emptySet(),
                    java.util.Collections.emptySet(),
                    java.util.Collections.emptySet(),
                    java.util.Collections.emptySet(),
                    java.util.Collections.emptySet(),
                    java.util.Collections.emptySet(),
                    java.util.Collections.emptySet()
            );
        }
    }
}
