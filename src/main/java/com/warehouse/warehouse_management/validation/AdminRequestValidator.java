package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.dto.CreateAdminRequest;
import com.warehouse.warehouse_management.persistence.UserPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminRequestValidator {

    private final AuthenticatedRequestValidator authenticatedRequestValidator;
    private final UserPersistenceService userPersistenceService;

    public void validateCreateAdmin(CreateAdminRequest request) {
        authenticatedRequestValidator.requireRole("SUPER_ADMIN");

        if (userPersistenceService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
    }
}
