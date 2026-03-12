package com.warehouse.warehouse_management.repository;

import com.warehouse.warehouse_management.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}