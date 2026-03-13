package com.warehouse.warehouse_management.dto;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        Double unitPrice,
        Double lineTotal
) {
}
