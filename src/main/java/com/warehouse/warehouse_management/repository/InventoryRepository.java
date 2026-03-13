package com.warehouse.warehouse_management.repository;

import com.warehouse.warehouse_management.dto.InventoryViewResponse;
import com.warehouse.warehouse_management.dto.LowStockItemResponse;
import com.warehouse.warehouse_management.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<Inventory> findByProductId(Long productId);

    List<Inventory> findByWarehouseId(Long warehouseId);

    @Query("""
            select new com.warehouse.warehouse_management.dto.InventoryViewResponse(
                i.id,
                p.id,
                p.name,
                p.sku,
                coalesce(p.reorderLevel, 5),
                w.id,
                w.name,
                w.location,
                i.quantity
            )
            from Inventory i
            join i.product p
            join i.warehouse w
            order by p.name asc, w.name asc, i.id asc
            """)
    Page<InventoryViewResponse> findInventoryView(Pageable pageable);

    @Query("""
            select new com.warehouse.warehouse_management.dto.InventoryViewResponse(
                i.id,
                p.id,
                p.name,
                p.sku,
                coalesce(p.reorderLevel, 5),
                w.id,
                w.name,
                w.location,
                i.quantity
            )
            from Inventory i
            join i.product p
            join i.warehouse w
            order by p.name asc, w.name asc, i.id asc
            """)
    List<InventoryViewResponse> findInventoryView();

    @Query("""
            select new com.warehouse.warehouse_management.dto.LowStockItemResponse(
                p.id,
                p.name,
                p.sku,
                coalesce(p.reorderLevel, 5),
                w.id,
                w.name,
                w.location,
                i.quantity
            )
            from Inventory i
            join i.product p
            join i.warehouse w
            where i.quantity < coalesce(p.reorderLevel, 5)
            order by (coalesce(p.reorderLevel, 5) - i.quantity) desc, p.name asc, w.name asc, i.id asc
            """)
    List<LowStockItemResponse> findLowStock();

    @Query("""
            select count(i)
            from Inventory i
            join i.product p
            where i.quantity < coalesce(p.reorderLevel, 5)
            """)
    long countLowStock();

    @Query("select coalesce(sum(i.quantity), 0) from Inventory i")
    Long sumAllQuantities();

}
