package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.entity.Product;

public record ValidatedOrderItem(
        Product product,
        Integer quantity
) {
}
