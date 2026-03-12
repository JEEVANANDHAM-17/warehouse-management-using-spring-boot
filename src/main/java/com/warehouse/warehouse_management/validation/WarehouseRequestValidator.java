package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.entity.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class WarehouseRequestValidator {

    private final AuthenticatedRequestValidator authenticatedRequestValidator;

    public void validateCreateWarehouse(Warehouse warehouse) {
        validateWarehouseAccess();

        if (warehouse == null) {
            throw new IllegalArgumentException("Warehouse payload is required");
        }

        if (!StringUtils.hasText(warehouse.getName())) {
            throw new IllegalArgumentException("Warehouse name is required");
        }

        if (!StringUtils.hasText(warehouse.getLocation())) {
            throw new IllegalArgumentException("Warehouse location is required");
        }
    }

    public void validateWarehouseAccess() {
        authenticatedRequestValidator.requireRole("ADMIN", "SUPER_ADMIN");
    }
}
