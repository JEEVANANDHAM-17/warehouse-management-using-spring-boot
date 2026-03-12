package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.dto.StockRequest;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.persistence.ProductPersistenceService;
import com.warehouse.warehouse_management.persistence.WarehousePersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryRequestValidator {

    private final AuthenticatedRequestValidator authenticatedRequestValidator;
    private final ProductPersistenceService productPersistenceService;
    private final WarehousePersistenceService warehousePersistenceService;

    public ValidatedStockRequest validateAddStock(StockRequest request) {
        authenticatedRequestValidator.requireRole("MANAGER", "ADMIN", "SUPER_ADMIN");

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Product product = productPersistenceService.getRequiredById(request.getProductId());
        Warehouse warehouse = warehousePersistenceService.getRequiredById(request.getWarehouseId());

        return new ValidatedStockRequest(product, warehouse, request.getQuantity());
    }
}
