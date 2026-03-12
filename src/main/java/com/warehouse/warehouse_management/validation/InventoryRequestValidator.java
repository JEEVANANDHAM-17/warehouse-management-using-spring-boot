package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.dto.StockRequest;
import com.warehouse.warehouse_management.entity.Inventory;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.persistence.InventoryPersistenceService;
import com.warehouse.warehouse_management.persistence.ProductPersistenceService;
import com.warehouse.warehouse_management.persistence.WarehousePersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryRequestValidator {

    private final AuthenticatedRequestValidator authenticatedRequestValidator;
    private final InventoryPersistenceService inventoryPersistenceService;
    private final ProductPersistenceService productPersistenceService;
    private final WarehousePersistenceService warehousePersistenceService;

    public ValidatedStockRequest validateAddStock(StockRequest request) {
        validateManageAccess();
        validateQuantity(request.getQuantity());

        Product product = getValidatedProduct(request.getProductId());
        Warehouse warehouse = getValidatedWarehouse(request.getWarehouseId());
        return new ValidatedStockRequest(product, warehouse, request.getQuantity());
    }

    public void validateReadAccess() {
        authenticatedRequestValidator.requireUser();
    }

    public Inventory validateUpdateInventory(Long inventoryId, StockRequest request) {
        validateManageAccess();
        validateQuantity(request.getQuantity());

        Product product = getValidatedProduct(request.getProductId());
        Warehouse warehouse = getValidatedWarehouse(request.getWarehouseId());
        Inventory inventory = inventoryPersistenceService.getRequiredById(inventoryId);

        inventoryPersistenceService.findByProductAndWarehouse(product.getId(), warehouse.getId())
                .filter(existingInventory -> !existingInventory.getId().equals(inventoryId))
                .ifPresent(existingInventory -> {
                    throw new IllegalArgumentException("Inventory already exists for this product and warehouse");
                });

        return inventory;
    }

    public Product getValidatedProduct(Long productId) {
        return productPersistenceService.getRequiredById(productId);
    }

    public Warehouse getValidatedWarehouse(Long warehouseId) {
        return warehousePersistenceService.getRequiredById(warehouseId);
    }

    private void validateManageAccess() {
        authenticatedRequestValidator.requireRole("MANAGER", "ADMIN", "SUPER_ADMIN");
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
}
