package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.dto.RegisterRequest;
import com.warehouse.warehouse_management.persistence.RolePersistenceService;
import com.warehouse.warehouse_management.persistence.UserPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthRequestValidator {

    private final UserPersistenceService userPersistenceService;
    private final RolePersistenceService rolePersistenceService;
    private final AuthenticatedRequestValidator authenticatedRequestValidator;

    public void validateRegister(RegisterRequest request) {
        authenticatedRequestValidator.requireRole("SUPER_ADMIN");

        if ("ADMIN".equalsIgnoreCase(request.getRole())
                || "SUPER_ADMIN".equalsIgnoreCase(request.getRole())) {
            throw new IllegalArgumentException("Admin creation is restricted");
        }

        if (userPersistenceService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        rolePersistenceService.getRequiredByName(request.getRole());
    }
}
