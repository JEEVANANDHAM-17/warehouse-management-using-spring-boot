package com.warehouse.warehouse_management.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long warehouseId,
        String warehouseName,
        String customerName,
        String status,
        Double totalAmount,
        String createdByName,
        String createdByEmail,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
