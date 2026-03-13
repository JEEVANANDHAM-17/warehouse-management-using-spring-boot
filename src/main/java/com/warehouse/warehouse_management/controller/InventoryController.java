package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.dto.InventoryViewResponse;
import com.warehouse.warehouse_management.dto.LowStockItemResponse;
import com.warehouse.warehouse_management.dto.PageResponse;
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

    @GetMapping("/view")
    public PageResponse<InventoryViewResponse> getView(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return inventoryService.getInventoryView(page, size);
    }

    @GetMapping("/{id}")
    public Inventory getById(@PathVariable Long id) {
        return inventoryService.getInventory(id);
    }

    @PutMapping("/{id}")
    public Inventory update(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return inventoryService.updateInventory(id, request);
    }

    @PostMapping("/remove-stock")
    public Inventory removeStock(@Valid @RequestBody StockRequest request) {

        return inventoryService.removeStock(request);
    }

    @GetMapping("/product/{productId}")
    public List<Inventory> getByProduct(@PathVariable Long productId) {
        return inventoryService.getInventoryByProduct(productId);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public List<Inventory> getByWarehouse(@PathVariable Long warehouseId) {
        return inventoryService.getInventoryByWarehouse(warehouseId);
    }

    @GetMapping("/low-stock")
    public List<LowStockItemResponse> getLowStock() {
        return inventoryService.getLowStock();
    }

}
