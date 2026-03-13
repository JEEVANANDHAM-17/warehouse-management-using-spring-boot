package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.persistence.WarehousePersistenceService;
import com.warehouse.warehouse_management.validation.WarehouseRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehousePersistenceService warehousePersistenceService;
    private final WarehouseRequestValidator warehouseRequestValidator;

    @Transactional
    @CacheEvict(cacheNames = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    public Warehouse createWarehouse(Warehouse warehouse) {
        warehouseRequestValidator.validateCreateWarehouse(warehouse);
        return warehousePersistenceService.save(warehouse);
    }

    public List<Warehouse> getAllWarehouses() {
        warehouseRequestValidator.validateReadAccess();
        return warehousePersistenceService.findAll();
    }

    public Warehouse getWarehouse(Long warehouseId) {
        warehouseRequestValidator.validateReadAccess();
        return warehousePersistenceService.getRequiredById(warehouseId);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    public Warehouse updateWarehouse(Long warehouseId, Warehouse request) {
        Warehouse warehouse = warehouseRequestValidator.validateUpdateWarehouse(warehouseId, request);
        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        return warehousePersistenceService.save(warehouse);
    }
}
