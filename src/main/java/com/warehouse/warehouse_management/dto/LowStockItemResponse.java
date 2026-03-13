package com.warehouse.warehouse_management.dto;

public record LowStockItemResponse(
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
