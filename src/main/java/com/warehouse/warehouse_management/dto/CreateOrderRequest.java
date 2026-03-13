package com.warehouse.warehouse_management.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "Warehouse id is required")
        Long warehouseId,

        @Size(max = 255, message = "Customer name must be less than 255 characters")
        String customerName,

        @NotEmpty(message = "At least one order item is required")
        @Valid
        List<OrderItemRequest> items
) {
}
