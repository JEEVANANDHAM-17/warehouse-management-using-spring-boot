package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.persistence.WarehousePersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class WarehouseRequestValidator {

    private final AuthenticatedRequestValidator authenticatedRequestValidator;
    private final WarehousePersistenceService warehousePersistenceService;

    public void validateCreateWarehouse(Warehouse warehouse) {
        validateManageAccess();
        validateWarehousePayload(warehouse);
    }

    public void validateReadAccess() {
        authenticatedRequestValidator.requireUser();
    }

    public Warehouse validateUpdateWarehouse(Long warehouseId, Warehouse warehouse) {
        validateManageAccess();
        validateWarehousePayload(warehouse);

        return warehousePersistenceService.getRequiredById(warehouseId);
    }

    private void validateManageAccess() {
        authenticatedRequestValidator.requireRole("ADMIN", "SUPER_ADMIN");
    }

    private void validateWarehousePayload(Warehouse warehouse) {
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
}
