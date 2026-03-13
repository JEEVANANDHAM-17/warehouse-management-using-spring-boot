package com.warehouse.warehouse_management.repository;

import com.warehouse.warehouse_management.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<Inventory> findByProductId(Long productId);

    List<Inventory> findByWarehouseId(Long warehouseId);

    List<Inventory> findByQuantityLessThan(Integer threshold);

    long countByQuantityLessThan(Integer threshold);

    @Query("select coalesce(sum(i.quantity), 0) from Inventory i")
    Long sumAllQuantities();

}
