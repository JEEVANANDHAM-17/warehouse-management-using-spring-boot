package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.dto.StockRequest;
import com.warehouse.warehouse_management.entity.Inventory;
import com.warehouse.warehouse_management.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/add-stock")
    public Inventory addStock(@Valid @RequestBody StockRequest request) {

        return inventoryService.addStock(request);
    }

    @GetMapping
    public List<Inventory> getAll() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/{id}")
    public Inventory getById(@PathVariable Long id) {
        return inventoryService.getInventory(id);
    }

    @PutMapping("/{id}")
    public Inventory update(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return inventoryService.updateInventory(id, request);
    }
}
