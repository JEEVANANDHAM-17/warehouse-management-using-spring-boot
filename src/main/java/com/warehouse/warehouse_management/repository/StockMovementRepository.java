package com.warehouse.warehouse_management.repository;

import com.warehouse.warehouse_management.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}