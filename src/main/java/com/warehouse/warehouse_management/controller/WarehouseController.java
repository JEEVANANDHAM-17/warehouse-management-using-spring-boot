package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public Warehouse create(@RequestBody Warehouse warehouse) {
        return service.createWarehouse(warehouse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping
    public List<Warehouse> getAll() {
        return service.getAllWarehouses();
    }
}