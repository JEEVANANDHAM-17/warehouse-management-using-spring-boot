package com.warehouse.warehouse_management.dto;

import java.time.LocalDateTime;

public record OrderSummaryResponse(
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
        Integer totalItems
) {
}
