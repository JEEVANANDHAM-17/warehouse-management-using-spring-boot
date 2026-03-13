package com.warehouse.warehouse_management.persistence;

import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehousePersistenceService {

    private final WarehouseRepository warehouseRepository;

    public Warehouse getRequiredById(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
    }

    public Warehouse save(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }

    public long countAll() {
        return warehouseRepository.count();
    }
}
