package com.warehouse.warehouse_management.dto;

public record LowStockItemResponse(
        Long productId,
        String productName,
        String productSku,
        Long warehouseId,
        String warehouseName,
        String warehouseLocation,
        Integer quantity,
        Integer threshold
) {
}
