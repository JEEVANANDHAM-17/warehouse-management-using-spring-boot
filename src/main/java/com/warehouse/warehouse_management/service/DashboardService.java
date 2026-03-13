package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.DashboardSummaryResponse;
import com.warehouse.warehouse_management.persistence.ProductPersistenceService;
import com.warehouse.warehouse_management.persistence.WarehousePersistenceService;
import com.warehouse.warehouse_management.validation.AuthenticatedRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    private final ProductPersistenceService productPersistenceService;
    private final WarehousePersistenceService warehousePersistenceService;
    private final InventoryService inventoryService;
    private final AuthenticatedRequestValidator authenticatedRequestValidator;

    public DashboardSummaryResponse getSummary(Integer threshold) {
        authenticatedRequestValidator.requireUser();

        int resolvedThreshold = threshold != null ? threshold : DEFAULT_LOW_STOCK_THRESHOLD;

        return new DashboardSummaryResponse(
                productPersistenceService.countAll(),
                warehousePersistenceService.countAll(),
                inventoryService.getTotalInventoryQuantity(),
                inventoryService.countLowStock(resolvedThreshold)
        );
    }
}
