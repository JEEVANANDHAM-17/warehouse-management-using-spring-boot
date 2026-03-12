package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.persistence.WarehousePersistenceService;
import com.warehouse.warehouse_management.validation.WarehouseRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehousePersistenceService warehousePersistenceService;
    private final WarehouseRequestValidator warehouseRequestValidator;

    public Warehouse createWarehouse(Warehouse warehouse) {
        warehouseRequestValidator.validateCreateWarehouse(warehouse);
        return warehousePersistenceService.save(warehouse);
    }

    public List<Warehouse> getAllWarehouses() {
        warehouseRequestValidator.validateWarehouseAccess();
        return warehousePersistenceService.findAll();
    }
}
