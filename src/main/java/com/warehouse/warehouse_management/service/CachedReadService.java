package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.DashboardSummaryResponse;
import com.warehouse.warehouse_management.dto.InventoryViewResponse;
import com.warehouse.warehouse_management.dto.LowStockItemResponse;
import com.warehouse.warehouse_management.dto.PageResponse;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.persistence.InventoryPersistenceService;
import com.warehouse.warehouse_management.persistence.ProductPersistenceService;
import com.warehouse.warehouse_management.persistence.WarehousePersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CachedReadService {

    private final ProductPersistenceService productPersistenceService;
    private final InventoryPersistenceService inventoryPersistenceService;
    private final WarehousePersistenceService warehousePersistenceService;

    @Cacheable(cacheNames = CacheNames.PRODUCTS)
    public PageResponse<Product> getProducts(String name, String sku, int page, int size) {
        Page<Product> result = productPersistenceService.search(name, sku, PageRequest.of(page, size));
        return PageResponse.from(result);
    }

    @Cacheable(cacheNames = CacheNames.INVENTORY_VIEW)
    public PageResponse<InventoryViewResponse> getInventoryView(int page, int size) {
        Page<InventoryViewResponse> result = inventoryPersistenceService.findInventoryView(PageRequest.of(page, size));
        return PageResponse.from(result);
    }

    @Cacheable(cacheNames = CacheNames.LOW_STOCK)
    public List<LowStockItemResponse> getLowStock() {
        return inventoryPersistenceService.findLowStock();
    }

    @Cacheable(cacheNames = CacheNames.DASHBOARD_SUMMARY)
    public DashboardSummaryResponse getDashboardSummary() {
        return new DashboardSummaryResponse(
                productPersistenceService.countAll(),
                warehousePersistenceService.countAll(),
                inventoryPersistenceService.sumAllQuantities(),
                inventoryPersistenceService.countLowStock()
        );
    }
}
