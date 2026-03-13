package com.warehouse.warehouse_management.event;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        Long warehouseId,
        String warehouseName,
        Integer totalItems,
        Double totalAmount,
        String createdByEmail,
        LocalDateTime createdAt
) {
}
