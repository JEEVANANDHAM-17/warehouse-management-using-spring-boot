package com.warehouse.warehouse_management.persistence;

import com.warehouse.warehouse_management.entity.Role;
import com.warehouse.warehouse_management.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RolePersistenceService {

    private final RoleRepository roleRepository;

    public Role getRequiredByName(String roleName) {
        return roleRepository.findByName(normalizeRole(roleName))
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
    }

    private String normalizeRole(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("Role is required");
        }

        return roleName.trim().toUpperCase(Locale.ROOT);
    }
}
