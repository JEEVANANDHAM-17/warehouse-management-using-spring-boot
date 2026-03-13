package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.InventoryViewResponse;
import com.warehouse.warehouse_management.dto.LowStockItemResponse;
import com.warehouse.warehouse_management.dto.PageResponse;
import com.warehouse.warehouse_management.dto.StockRequest;
import com.warehouse.warehouse_management.entity.Inventory;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.entity.StockMovement;
import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.event.InventoryUpdatedEvent;
import com.warehouse.warehouse_management.event.LowStockDetectedEvent;
import com.warehouse.warehouse_management.persistence.InventoryPersistenceService;
import com.warehouse.warehouse_management.repository.StockMovementRepository;
import com.warehouse.warehouse_management.validation.AuthenticatedRequestValidator;
import com.warehouse.warehouse_management.validation.InventoryRequestValidator;
import com.warehouse.warehouse_management.validation.ValidatedStockRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final int DEFAULT_REORDER_LEVEL = 5;

    private final InventoryPersistenceService inventoryPersistenceService;
    private final InventoryRequestValidator inventoryRequestValidator;
    private final StockMovementRepository stockMovementRepository;
    private final AuthenticatedRequestValidator authenticatedRequestValidator;
    private final CachedReadService cachedReadService;
    private final AuditLogService auditLogService;
    private final WarehouseEventPublisher warehouseEventPublisher;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.INVENTORY_VIEW, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOW_STOCK, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    })
    public Inventory addStock(StockRequest request) {
        ValidatedStockRequest validatedRequest = inventoryRequestValidator.validateAddStock(request);
        User actor = authenticatedRequestValidator.requireUser();
        return adjustInventory(
                validatedRequest.product(),
                validatedRequest.warehouse(),
                validatedRequest.quantity(),
                "IN",
                "Manual stock addition",
                actor
        );
    }

    public List<Inventory> getAllInventory() {
        inventoryRequestValidator.validateReadAccess();
        return inventoryPersistenceService.findAll();
    }

    public PageResponse<InventoryViewResponse> getInventoryView(int page, int size) {
        inventoryRequestValidator.validateReadAccess();
        validatePageRequest(page, size);
        return cachedReadService.getInventoryView(page, size);
    }

    public Inventory getInventory(Long inventoryId) {
        inventoryRequestValidator.validateReadAccess();
        return inventoryPersistenceService.getRequiredById(inventoryId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.INVENTORY_VIEW, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOW_STOCK, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    })
    public Inventory updateInventory(Long inventoryId, StockRequest request) {
        Inventory inventory = inventoryRequestValidator.validateUpdateInventory(inventoryId, request);
        User actor = authenticatedRequestValidator.requireUser();
        Product previousProduct = inventory.getProduct();
        Warehouse previousWarehouse = inventory.getWarehouse();
        int previousQuantity = inventory.getQuantity();

        inventory.setProduct(inventoryRequestValidator.getValidatedProduct(request.getProductId()));
        inventory.setWarehouse(inventoryRequestValidator.getValidatedWarehouse(request.getWarehouseId()));
        inventory.setQuantity(request.getQuantity());

        Inventory savedInventory = inventoryPersistenceService.save(inventory);
        int delta = savedInventory.getQuantity() - previousQuantity;

        if (delta != 0) {
            stockMovementRepository.save(StockMovement.builder()
                    .product(savedInventory.getProduct())
                    .warehouse(savedInventory.getWarehouse())
                    .quantity(Math.abs(delta))
                    .type("ADJUSTMENT")
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        auditLogService.log(
                "INVENTORY_UPDATED",
                "INVENTORY",
                savedInventory.getId(),
                "Updated inventory from "
                        + previousProduct.getSku()
                        + "@"
                        + previousWarehouse.getName()
                        + " qty "
                        + previousQuantity
                        + " to "
                        + savedInventory.getProduct().getSku()
                        + "@"
                        + savedInventory.getWarehouse().getName()
                        + " qty "
                        + savedInventory.getQuantity(),
                actor
        );

        publishInventoryUpdated(savedInventory, previousQuantity, delta, "ADJUSTMENT", "Manual inventory update");
        publishLowStockIfNeeded(savedInventory);
        return savedInventory;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.INVENTORY_VIEW, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOW_STOCK, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    })
    public Inventory removeStock(StockRequest request) {
        ValidatedStockRequest validatedRequest = inventoryRequestValidator.validateAddStock(request);
        User actor = authenticatedRequestValidator.requireUser();
        return adjustInventory(
                validatedRequest.product(),
                validatedRequest.warehouse(),
                -validatedRequest.quantity(),
                "OUT",
                "Manual stock removal",
                actor
        );
    }

    public List<Inventory> getInventoryByProduct(Long productId) {
        inventoryRequestValidator.validateReadAccess();
        return inventoryPersistenceService.findByProductId(productId);
    }

    public List<Inventory> getInventoryByWarehouse(Long warehouseId) {
        inventoryRequestValidator.validateReadAccess();
        return inventoryPersistenceService.findByWarehouseId(warehouseId);
    }

    public List<LowStockItemResponse> getLowStock() {
        inventoryRequestValidator.validateReadAccess();
        return cachedReadService.getLowStock();
    }

    public long countLowStock() {
        inventoryRequestValidator.validateReadAccess();
        return inventoryPersistenceService.countLowStock();
    }

    public long getTotalInventoryQuantity() {
        inventoryRequestValidator.validateReadAccess();
        return inventoryPersistenceService.sumAllQuantities();
    }

    Inventory deductStockForOrder(Product product, Warehouse warehouse, Integer quantity, String orderNumber, User actor) {
        return adjustInventory(product, warehouse, -quantity, "OUT", "Order " + orderNumber, actor);
    }

    private Inventory adjustInventory(Product product,
                                      Warehouse warehouse,
                                      int delta,
                                      String movementType,
                                      String reason,
                                      User actor) {
        Inventory inventory = inventoryPersistenceService
                .findByProductAndWarehouse(product.getId(), warehouse.getId())
                .orElse(null);

        if (inventory == null && delta < 0) {
            throw new IllegalArgumentException("Inventory not found for product " + product.getSku() + " in warehouse " + warehouse.getName());
        }

        int previousQuantity = inventory != null ? inventory.getQuantity() : 0;
        int updatedQuantity = previousQuantity + delta;

        if (updatedQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock for product " + product.getSku() + " in warehouse " + warehouse.getName());
        }

        if (inventory == null) {
            inventory = Inventory.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .quantity(updatedQuantity)
                    .build();
        } else {
            inventory.setQuantity(updatedQuantity);
        }

        Inventory savedInventory = inventoryPersistenceService.save(inventory);
        stockMovementRepository.save(StockMovement.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(Math.abs(delta))
                .type(movementType)
                .createdAt(LocalDateTime.now())
                .build());

        auditLogService.log(
                "INVENTORY_UPDATED",
                "INVENTORY",
                savedInventory.getId(),
                "Inventory changed for product "
                        + product.getSku()
                        + " in warehouse "
                        + warehouse.getName()
                        + ". Previous quantity: "
                        + previousQuantity
                        + ", current quantity: "
                        + updatedQuantity
                        + ", reason: "
                        + reason,
                actor
        );

        publishInventoryUpdated(savedInventory, previousQuantity, delta, movementType, reason);
        publishLowStockIfNeeded(savedInventory);
        return savedInventory;
    }

    private void publishInventoryUpdated(Inventory inventory,
                                         int previousQuantity,
                                         int delta,
                                         String changeType,
                                         String reason) {
        warehouseEventPublisher.publishInventoryUpdated(new InventoryUpdatedEvent(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                inventory.getProduct().getSku(),
                inventory.getWarehouse().getId(),
                inventory.getWarehouse().getName(),
                previousQuantity,
                inventory.getQuantity(),
                delta,
                changeType,
                reason,
                LocalDateTime.now()
        ));
    }

    private void publishLowStockIfNeeded(Inventory inventory) {
        int reorderLevel = resolveReorderLevel(inventory.getProduct().getReorderLevel());

        if (inventory.getQuantity() < reorderLevel) {
            warehouseEventPublisher.publishLowStockDetected(new LowStockDetectedEvent(
                    inventory.getProduct().getId(),
                    inventory.getProduct().getName(),
                    inventory.getProduct().getSku(),
                    inventory.getWarehouse().getId(),
                    inventory.getWarehouse().getName(),
                    inventory.getQuantity(),
                    reorderLevel,
                    LocalDateTime.now()
            ));
        }
    }

    private int resolveReorderLevel(Integer reorderLevel) {
        if (reorderLevel == null || reorderLevel <= 0) {
            return DEFAULT_REORDER_LEVEL;
        }

        return reorderLevel;
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to 0");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
    }
}
