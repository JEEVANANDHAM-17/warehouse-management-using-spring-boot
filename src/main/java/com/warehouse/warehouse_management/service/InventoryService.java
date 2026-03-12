package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.StockRequest;
import com.warehouse.warehouse_management.entity.Inventory;
import com.warehouse.warehouse_management.entity.StockMovement;
import com.warehouse.warehouse_management.persistence.InventoryPersistenceService;
import com.warehouse.warehouse_management.repository.StockMovementRepository;
import com.warehouse.warehouse_management.validation.InventoryRequestValidator;
import com.warehouse.warehouse_management.validation.ValidatedStockRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryPersistenceService inventoryPersistenceService;
    private final InventoryRequestValidator inventoryRequestValidator;
    private final StockMovementRepository stockMovementRepository;

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

        inventory = inventoryPersistenceService.save(inventory);

        StockMovement movement = StockMovement.builder()
                .product(validatedRequest.product())
                .warehouse(validatedRequest.warehouse())
                .quantity(validatedRequest.quantity())
                .type("IN")
                .createdAt(LocalDateTime.now())
                .build();

        stockMovementRepository.save(movement);

        return inventory;
    }

    public List<Inventory> getAllInventory() {
        inventoryRequestValidator.validateReadAccess();
        return inventoryPersistenceService.findAll();
    }

    public Inventory getInventory(Long inventoryId) {
        inventoryRequestValidator.validateReadAccess();
        return inventoryPersistenceService.getRequiredById(inventoryId);
    }

    public Inventory updateInventory(Long inventoryId, StockRequest request) {
        Inventory inventory = inventoryRequestValidator.validateUpdateInventory(inventoryId, request);

        inventory.setProduct(inventoryRequestValidator.getValidatedProduct(request.getProductId()));
        inventory.setWarehouse(inventoryRequestValidator.getValidatedWarehouse(request.getWarehouseId()));
        inventory.setQuantity(request.getQuantity());

        return inventoryPersistenceService.save(inventory);
    }

    public Inventory removeStock(StockRequest request) {
        ValidatedStockRequest validatedRequest = inventoryRequestValidator.validateAddStock(request);

        Inventory inventory = inventoryPersistenceService
                .findByProductAndWarehouse(
                        validatedRequest.product().getId(),
                        validatedRequest.warehouse().getId()
                )
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (inventory.getQuantity() < validatedRequest.quantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        inventory.setQuantity(inventory.getQuantity() - validatedRequest.quantity());

        inventory = inventoryPersistenceService.save(inventory);

        StockMovement movement = StockMovement.builder()
                .product(inventory.getProduct())
                .warehouse(inventory.getWarehouse())
                .quantity(validatedRequest.quantity())
                .type("OUT")
                .createdAt(LocalDateTime.now())
                .build();

        stockMovementRepository.save(movement);

        return inventory;
    }

}
