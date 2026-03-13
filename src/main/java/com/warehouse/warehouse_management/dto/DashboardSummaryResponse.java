package com.warehouse.warehouse_management.dto;

public record DashboardSummaryResponse(
        long totalProducts,
        long totalWarehouses,
        long totalInventory,
        long lowStockCount
) {
}
