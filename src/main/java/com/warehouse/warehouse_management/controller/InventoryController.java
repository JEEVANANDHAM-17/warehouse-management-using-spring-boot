package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.dto.StockRequest;
import com.warehouse.warehouse_management.entity.Inventory;
import com.warehouse.warehouse_management.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPER_ADMIN')")
    @PostMapping("/add-stock")
    public Inventory addStock(@Valid @RequestBody StockRequest request) {

        return inventoryService.addStock(request);
    }
}