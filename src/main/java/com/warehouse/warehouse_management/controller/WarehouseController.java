package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;

    @PostMapping
    public Warehouse create(@RequestBody Warehouse warehouse) {
        return service.createWarehouse(warehouse);
    }

    @GetMapping
    public List<Warehouse> getAll() {
        return service.getAllWarehouses();
    }

    @GetMapping("/{id}")
    public Warehouse getById(@PathVariable Long id) {
        return service.getWarehouse(id);
    }

    @PutMapping("/{id}")
    public Warehouse update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        return service.updateWarehouse(id, warehouse);
    }
}
