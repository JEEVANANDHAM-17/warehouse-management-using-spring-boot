package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.StockRequest;
import com.warehouse.warehouse_management.entity.Inventory;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.repository.InventoryRepository;
import com.warehouse.warehouse_management.repository.ProductRepository;
import com.warehouse.warehouse_management.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public Inventory addStock(StockRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .orElse(null);

        if (inventory == null) {

            inventory = Inventory.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .quantity(request.getQuantity())
                    .build();

        } else {

            inventory.setQuantity(inventory.getQuantity() + request.getQuantity());

        }

        return inventoryRepository.save(inventory);
    }

}