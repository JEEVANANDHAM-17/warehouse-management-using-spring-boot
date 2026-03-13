package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.dto.CreateOrderRequest;
import com.warehouse.warehouse_management.dto.OrderItemRequest;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.entity.Warehouse;
import com.warehouse.warehouse_management.persistence.ProductPersistenceService;
import com.warehouse.warehouse_management.persistence.WarehousePersistenceService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderRequestValidator {

    private final AuthenticatedRequestValidator authenticatedRequestValidator;
    private final ProductPersistenceService productPersistenceService;
    private final WarehousePersistenceService warehousePersistenceService;

    public OrderRequestValidator(AuthenticatedRequestValidator authenticatedRequestValidator,
                                 ProductPersistenceService productPersistenceService,
                                 WarehousePersistenceService warehousePersistenceService) {
        this.authenticatedRequestValidator = authenticatedRequestValidator;
        this.productPersistenceService = productPersistenceService;
        this.warehousePersistenceService = warehousePersistenceService;
    }

    public ValidatedOrderRequest validateCreateOrder(CreateOrderRequest request) {
        User actor = authenticatedRequestValidator.requireRole("STAFF", "MANAGER", "ADMIN", "SUPER_ADMIN");
        Warehouse warehouse = warehousePersistenceService.getRequiredById(request.warehouseId());

        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required");
        }

        Map<Long, ValidatedOrderItem> aggregatedItems = new LinkedHashMap<>();

        for (OrderItemRequest itemRequest : request.items()) {
            if (itemRequest.quantity() == null || itemRequest.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

            Product product = productPersistenceService.getRequiredById(itemRequest.productId());

            aggregatedItems.merge(
                    product.getId(),
                    new ValidatedOrderItem(product, itemRequest.quantity()),
                    (existing, incoming) -> new ValidatedOrderItem(existing.product(), existing.quantity() + incoming.quantity())
            );
        }

        String customerName = StringUtils.hasText(request.customerName()) ? request.customerName().trim() : null;
        return new ValidatedOrderRequest(actor, warehouse, customerName, new ArrayList<>(aggregatedItems.values()));
    }

    public void validateReadAccess() {
        authenticatedRequestValidator.requireUser();
    }
}
