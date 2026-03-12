package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.entity.Warehouse;

public record ValidatedStockRequest(Product product, Warehouse warehouse, Integer quantity) {
}
