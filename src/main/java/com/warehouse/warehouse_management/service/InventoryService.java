package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.StockRequest;
import com.warehouse.warehouse_management.entity.Inventory;
import com.warehouse.warehouse_management.persistence.InventoryPersistenceService;
import com.warehouse.warehouse_management.validation.InventoryRequestValidator;
import com.warehouse.warehouse_management.validation.ValidatedStockRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryPersistenceService inventoryPersistenceService;
    private final InventoryRequestValidator inventoryRequestValidator;

    public Inventory addStock(StockRequest request) {
        ValidatedStockRequest validatedRequest = inventoryRequestValidator.validateAddStock(request);

        Inventory inventory = inventoryPersistenceService
                .findByProductAndWarehouse(
                        validatedRequest.product().getId(),
                        validatedRequest.warehouse().getId()
                )
                .orElse(null);

        if (inventory == null) {

            inventory = Inventory.builder()
                    .product(validatedRequest.product())
                    .warehouse(validatedRequest.warehouse())
                    .quantity(validatedRequest.quantity())
                    .build();

        } else {

            inventory.setQuantity(inventory.getQuantity() + validatedRequest.quantity());

        }

        return inventoryPersistenceService.save(inventory);
    }

}
