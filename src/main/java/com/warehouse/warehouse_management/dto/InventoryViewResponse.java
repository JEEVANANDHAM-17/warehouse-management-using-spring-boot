package com.warehouse.warehouse_management.dto;

public record InventoryViewResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer reorderLevel,
        Long warehouseId,
        String warehouseName,
        String warehouseLocation,
        Integer quantity
) {
}
