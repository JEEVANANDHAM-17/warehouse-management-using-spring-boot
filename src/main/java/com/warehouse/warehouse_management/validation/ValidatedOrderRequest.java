package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.entity.Warehouse;

import java.util.List;

public record ValidatedOrderRequest(
        User actor,
        Warehouse warehouse,
        String customerName,
        List<ValidatedOrderItem> items
) {
}
