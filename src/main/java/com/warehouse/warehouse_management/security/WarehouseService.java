package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository repository;

    public Warehouse createWarehouse(Warehouse warehouse) {
        return repository.save(warehouse);
    }

    public List<Warehouse> getAllWarehouses() {
        return repository.findAll();
    }
}