package com.warehouse.warehouse_management.event;

import java.time.LocalDateTime;

public record LowStockDetectedEvent(
        Long productId,
        String productName,
        String productSku,
        Long warehouseId,
        String warehouseName,
        Integer currentQuantity,
        Integer reorderLevel,
        LocalDateTime occurredAt
) {
}
