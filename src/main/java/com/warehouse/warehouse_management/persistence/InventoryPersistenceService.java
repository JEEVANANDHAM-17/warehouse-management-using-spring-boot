package com.warehouse.warehouse_management.persistence;

import com.warehouse.warehouse_management.dto.InventoryViewResponse;
import com.warehouse.warehouse_management.dto.LowStockItemResponse;
import com.warehouse.warehouse_management.entity.Inventory;
import com.warehouse.warehouse_management.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryPersistenceService {

    private final InventoryRepository inventoryRepository;

    public Optional<Inventory> findByProductAndWarehouse(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }

    public Inventory getRequiredById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public List<Inventory> findByWarehouseId(Long warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId);
    }

    public List<InventoryViewResponse> findInventoryView() {
        return inventoryRepository.findInventoryView();
    }

    public List<LowStockItemResponse> findLowStock() {
        return inventoryRepository.findLowStock();
    }

    public long countLowStock() {
        return inventoryRepository.countLowStock();
    }

    public long sumAllQuantities() {
        return inventoryRepository.sumAllQuantities();
    }

    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }
}
