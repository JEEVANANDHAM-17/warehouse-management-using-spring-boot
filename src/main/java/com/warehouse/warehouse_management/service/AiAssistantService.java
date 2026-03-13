package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.AiQueryResponse;
import com.warehouse.warehouse_management.dto.DashboardSummaryResponse;
import com.warehouse.warehouse_management.dto.LowStockItemResponse;
import com.warehouse.warehouse_management.dto.OrderSummaryResponse;
import com.warehouse.warehouse_management.entity.Inventory;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.persistence.InventoryPersistenceService;
import com.warehouse.warehouse_management.persistence.OrderPersistenceService;
import com.warehouse.warehouse_management.persistence.ProductPersistenceService;
import com.warehouse.warehouse_management.validation.AuthenticatedRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final AuthenticatedRequestValidator authenticatedRequestValidator;
    private final CachedReadService cachedReadService;
    private final ProductPersistenceService productPersistenceService;
    private final InventoryPersistenceService inventoryPersistenceService;
    private final OrderService orderService;
    private final OrderPersistenceService orderPersistenceService;

    public AiQueryResponse answer(String query) {
        authenticatedRequestValidator.requireUser();

        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isBlank()) {
            throw new IllegalArgumentException("Query is required");
        }

        if (containsAny(normalizedQuery, "dashboard", "summary", "overview", "status")) {
            return buildSummaryResponse();
        }

        if (containsAny(normalizedQuery, "low stock", "reorder", "restock")) {
            return buildLowStockResponse();
        }

        if (containsAny(normalizedQuery, "recent order", "latest order", "last order")) {
            return buildRecentOrdersResponse(5);
        }

        if (containsAny(normalizedQuery, "order count", "how many orders", "total orders")) {
            long totalOrders = orderPersistenceService.countAll();
            return new AiQueryResponse(
                    "There are " + totalOrders + " orders in the system.",
                    List.of("Orders are counted from the `orders` table.", "Use `GET /orders` for the detailed order list."),
                    List.of(totalOrders == 0 ? "Start by creating the first order." : "Review the latest orders for fulfillment trends."),
                    LocalDateTime.now()
            );
        }

        Optional<Product> matchedProduct = findMentionedProduct(normalizedQuery);
        if (matchedProduct.isPresent() && containsAny(normalizedQuery, "inventory", "stock", "quantity", "available")) {
            return buildProductInventoryResponse(matchedProduct.get(), normalizedQuery);
        }

        return buildFallbackResponse(normalizedQuery);
    }

    private AiQueryResponse buildSummaryResponse() {
        DashboardSummaryResponse summary = cachedReadService.getDashboardSummary();
        long totalOrders = orderPersistenceService.countAll();

        List<String> insights = new ArrayList<>();
        insights.add("Products: " + summary.totalProducts());
        insights.add("Warehouses: " + summary.totalWarehouses());
        insights.add("Inventory units: " + summary.totalInventory());
        insights.add("Low-stock records: " + summary.lowStockCount());
        insights.add("Orders: " + totalOrders);

        List<String> suggestedActions = new ArrayList<>();
        if (summary.lowStockCount() > 0) {
            suggestedActions.add("Review `GET /inventory/low-stock` and replenish critical items.");
        }
        if (totalOrders > 0) {
            suggestedActions.add("Check `GET /orders` for the most recent fulfillment activity.");
        }
        if (suggestedActions.isEmpty()) {
            suggestedActions.add("Warehouse metrics look stable right now.");
        }

        return new AiQueryResponse(
                "Warehouse summary: "
                        + summary.totalProducts()
                        + " products, "
                        + summary.totalWarehouses()
                        + " warehouses, "
                        + summary.totalInventory()
                        + " total inventory units, "
                        + summary.lowStockCount()
                        + " low-stock records, and "
                        + totalOrders
                        + " orders.",
                insights,
                suggestedActions,
                LocalDateTime.now()
        );
    }

    private AiQueryResponse buildLowStockResponse() {
        List<LowStockItemResponse> lowStockItems = cachedReadService.getLowStock();
        if (lowStockItems.isEmpty()) {
            return new AiQueryResponse(
                    "No low-stock items are currently detected.",
                    List.of("All tracked inventory rows are at or above their reorder level."),
                    List.of("Keep monitoring `/inventory/low-stock` for new shortages."),
                    LocalDateTime.now()
            );
        }

        List<String> insights = lowStockItems.stream()
                .limit(5)
                .map(item -> item.productName()
                        + " (" + item.productSku() + ") at "
                        + item.warehouseName()
                        + ": "
                        + item.quantity()
                        + " units, reorder level "
                        + item.reorderLevel())
                .toList();

        return new AiQueryResponse(
                "There are " + lowStockItems.size() + " low-stock inventory records. "
                        + lowStockItems.get(0).productName()
                        + " at "
                        + lowStockItems.get(0).warehouseName()
                        + " is one of the most urgent items to replenish.",
                insights,
                List.of("Use `POST /inventory/add-stock` to replenish critical items.", "Review reorder levels on products if thresholds need tuning."),
                LocalDateTime.now()
        );
    }

    private AiQueryResponse buildRecentOrdersResponse(int limit) {
        List<OrderSummaryResponse> recentOrders = orderService.getAllOrders().stream()
                .limit(limit)
                .toList();

        if (recentOrders.isEmpty()) {
            return new AiQueryResponse(
                    "There are no orders in the system yet.",
                    List.of("Order history is currently empty."),
                    List.of("Create an order with `POST /orders` when fulfillment starts."),
                    LocalDateTime.now()
            );
        }

        List<String> insights = recentOrders.stream()
                .map(order -> order.orderNumber()
                        + " | warehouse: "
                        + order.warehouseName()
                        + " | items: "
                        + order.totalItems()
                        + " | total: "
                        + order.totalAmount())
                .toList();

        return new AiQueryResponse(
                "The latest order is " + recentOrders.get(0).orderNumber()
                        + " from " + recentOrders.get(0).warehouseName()
                        + " with total amount " + recentOrders.get(0).totalAmount() + ".",
                insights,
                List.of("Open `GET /orders/" + recentOrders.get(0).id() + "` for line-item details."),
                LocalDateTime.now()
        );
    }

    private AiQueryResponse buildProductInventoryResponse(Product product, String normalizedQuery) {
        List<Inventory> inventoryRows = inventoryPersistenceService.findByProductId(product.getId());
        if (inventoryRows.isEmpty()) {
            return new AiQueryResponse(
                    "No inventory is available for " + product.getName() + " (" + product.getSku() + ").",
                    List.of("The product exists but has no inventory rows."),
                    List.of("Use `POST /inventory/add-stock` to create initial stock."),
                    LocalDateTime.now()
            );
        }

        List<Inventory> filteredRows = filterByWarehouseMention(inventoryRows, normalizedQuery);
        List<Inventory> rowsToUse = filteredRows.isEmpty() ? inventoryRows : filteredRows;

        int totalQuantity = rowsToUse.stream()
                .mapToInt(Inventory::getQuantity)
                .sum();

        List<String> insights = rowsToUse.stream()
                .sorted(Comparator.comparing(row -> row.getWarehouse().getName(), String.CASE_INSENSITIVE_ORDER))
                .map(row -> row.getWarehouse().getName() + ": " + row.getQuantity() + " units")
                .toList();

        return new AiQueryResponse(
                product.getName() + " (" + product.getSku() + ") has " + totalQuantity + " units available"
                        + (rowsToUse.size() == inventoryRows.size() ? " across all warehouses." : " in the matched warehouse scope."),
                insights,
                List.of(totalQuantity <= resolveReorderLevel(product) ? "Restock this product soon." : "Current stock looks healthy."),
                LocalDateTime.now()
        );
    }

    private AiQueryResponse buildFallbackResponse(String normalizedQuery) {
        DashboardSummaryResponse summary = cachedReadService.getDashboardSummary();
        List<LowStockItemResponse> lowStockItems = cachedReadService.getLowStock();
        List<OrderSummaryResponse> recentOrders = orderService.getAllOrders().stream().limit(3).toList();

        List<String> insights = new ArrayList<>();
        insights.add("Current inventory units: " + summary.totalInventory());
        insights.add("Low-stock records: " + summary.lowStockCount());
        if (!recentOrders.isEmpty()) {
            insights.add("Latest order: " + recentOrders.get(0).orderNumber());
        }
        if (!lowStockItems.isEmpty()) {
            insights.add("Top low-stock item: " + lowStockItems.get(0).productName() + " at " + lowStockItems.get(0).warehouseName());
        }

        return new AiQueryResponse(
                "I can answer warehouse questions about summary metrics, low-stock items, product stock availability, and recent orders. "
                        + "I could not confidently map this query: `" + normalizedQuery + "`.",
                insights,
                List.of(
                        "Try asking: `Give me the warehouse summary`.",
                        "Try asking: `Which items are low on stock?`.",
                        "Try asking: `How much stock do we have for SKU ABC-123?`."
                ),
                LocalDateTime.now()
        );
    }

    private Optional<Product> findMentionedProduct(String normalizedQuery) {
        return productPersistenceService.findAll().stream()
                .filter(product -> {
                    String sku = product.getSku() != null ? product.getSku().toLowerCase(Locale.ROOT) : "";
                    String name = product.getName() != null ? product.getName().toLowerCase(Locale.ROOT) : "";
                    return (!sku.isBlank() && normalizedQuery.contains(sku))
                            || (!name.isBlank() && normalizedQuery.contains(name));
                })
                .findFirst();
    }

    private List<Inventory> filterByWarehouseMention(List<Inventory> inventoryRows, String normalizedQuery) {
        List<Inventory> matchedRows = inventoryRows.stream()
                .filter(row -> {
                    String warehouseName = row.getWarehouse().getName() != null
                            ? row.getWarehouse().getName().toLowerCase(Locale.ROOT)
                            : "";
                    String warehouseLocation = row.getWarehouse().getLocation() != null
                            ? row.getWarehouse().getLocation().toLowerCase(Locale.ROOT)
                            : "";
                    return (!warehouseName.isBlank() && normalizedQuery.contains(warehouseName))
                            || (!warehouseLocation.isBlank() && normalizedQuery.contains(warehouseLocation));
                })
                .toList();

        return matchedRows;
    }

    private int resolveReorderLevel(Product product) {
        if (product.getReorderLevel() == null || product.getReorderLevel() <= 0) {
            return 5;
        }

        return product.getReorderLevel();
    }

    private boolean containsAny(String value, String... phrases) {
        for (String phrase : phrases) {
            if (value.contains(phrase)) {
                return true;
            }
        }

        return false;
    }
}
