package com.warehouse.warehouse_management.event;

import java.time.LocalDateTime;

public record InventoryUpdatedEvent(
        Long inventoryId,
        Long productId,
        String productName,
        String productSku,
        Long warehouseId,
        String warehouseName,
        Integer previousQuantity,
        Integer currentQuantity,
        Integer delta,
        String changeType,
        String reason,
        LocalDateTime occurredAt
) {
}
