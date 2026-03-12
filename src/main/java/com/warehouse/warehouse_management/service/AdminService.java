package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.CreateAdminRequest;
import com.warehouse.warehouse_management.entity.Role;
import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.persistence.RolePersistenceService;
import com.warehouse.warehouse_management.persistence.UserPersistenceService;
import com.warehouse.warehouse_management.validation.AdminRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserPersistenceService userPersistenceService;
    private final RolePersistenceService rolePersistenceService;
    private final PasswordEncoder passwordEncoder;
    private final AdminRequestValidator adminRequestValidator;

    public void createAdmin(CreateAdminRequest request) {
        adminRequestValidator.validateCreateAdmin(request);

        Role role = rolePersistenceService.getRequiredByName("ADMIN");

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userPersistenceService.save(user);
    }
}
