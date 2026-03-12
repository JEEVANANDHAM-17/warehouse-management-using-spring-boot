package com.warehouse.warehouse_management.persistence;

import com.warehouse.warehouse_management.entity.Inventory;
import com.warehouse.warehouse_management.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryPersistenceService {

    private final InventoryRepository inventoryRepository;

    public Optional<Inventory> findByProductAndWarehouse(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }

    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }
}
