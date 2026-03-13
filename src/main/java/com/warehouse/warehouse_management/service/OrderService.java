package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.CreateOrderRequest;
import com.warehouse.warehouse_management.dto.OrderItemResponse;
import com.warehouse.warehouse_management.dto.OrderResponse;
import com.warehouse.warehouse_management.dto.OrderSummaryResponse;
import com.warehouse.warehouse_management.entity.CustomerOrder;
import com.warehouse.warehouse_management.entity.OrderItem;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.event.OrderCreatedEvent;
import com.warehouse.warehouse_management.persistence.OrderPersistenceService;
import com.warehouse.warehouse_management.validation.OrderRequestValidator;
import com.warehouse.warehouse_management.validation.ValidatedOrderItem;
import com.warehouse.warehouse_management.validation.ValidatedOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_STATUS_CREATED = "CREATED";

    private final OrderPersistenceService orderPersistenceService;
    private final OrderRequestValidator orderRequestValidator;
    private final InventoryService inventoryService;
    private final AuditLogService auditLogService;
    private final WarehouseEventPublisher warehouseEventPublisher;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.INVENTORY_VIEW, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOW_STOCK, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    })
    public OrderResponse createOrder(CreateOrderRequest request) {
        ValidatedOrderRequest validatedRequest = orderRequestValidator.validateCreateOrder(request);
        User actor = validatedRequest.actor();
        String orderNumber = generateOrderNumber();
        LocalDateTime createdAt = LocalDateTime.now();

        CustomerOrder order = CustomerOrder.builder()
                .orderNumber(orderNumber)
                .warehouse(validatedRequest.warehouse())
                .customerName(validatedRequest.customerName())
                .status(ORDER_STATUS_CREATED)
                .totalAmount(0D)
                .createdBy(actor)
                .createdAt(createdAt)
                .build();

        double totalAmount = 0D;
        int totalItems = 0;

        for (ValidatedOrderItem validatedItem : validatedRequest.items()) {
            Product product = validatedItem.product();
            int quantity = validatedItem.quantity();
            double unitPrice = product.getPrice() != null ? product.getPrice() : 0D;
            double lineTotal = unitPrice * quantity;

            inventoryService.deductStockForOrder(
                    product,
                    validatedRequest.warehouse(),
                    quantity,
                    orderNumber,
                    actor
            );

            order.addItem(OrderItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build());

            totalAmount += lineTotal;
            totalItems += quantity;
        }

        order.setTotalAmount(totalAmount);
        CustomerOrder savedOrder = orderPersistenceService.save(order);

        auditLogService.log(
                "ORDER_PLACED",
                "ORDER",
                savedOrder.getId(),
                "Placed order " + savedOrder.getOrderNumber()
                        + " for warehouse "
                        + savedOrder.getWarehouse().getName()
                        + " with "
                        + totalItems
                        + " items and total amount "
                        + savedOrder.getTotalAmount(),
                actor
        );

        warehouseEventPublisher.publishOrderCreated(new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getWarehouse().getId(),
                savedOrder.getWarehouse().getName(),
                totalItems,
                savedOrder.getTotalAmount(),
                actor.getEmail(),
                savedOrder.getCreatedAt()
        ));

        return mapDetailedResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getAllOrders() {
        orderRequestValidator.validateReadAccess();
        return orderPersistenceService.findAllSummaries().stream()
                .map(this::mapSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        orderRequestValidator.validateReadAccess();
        CustomerOrder order = orderPersistenceService.getRequiredDetailedById(orderId);
        return mapDetailedResponse(order);
    }

    private OrderSummaryResponse mapSummaryResponse(CustomerOrder order) {
        int totalItems = order.getItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getWarehouse().getId(),
                order.getWarehouse().getName(),
                order.getCustomerName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedBy().getName(),
                order.getCreatedBy().getEmail(),
                order.getCreatedAt(),
                totalItems
        );
    }

    private OrderResponse mapDetailedResponse(CustomerOrder order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getWarehouse().getId(),
                order.getWarehouse().getName(),
                order.getCustomerName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedBy().getName(),
                order.getCreatedBy().getEmail(),
                order.getCreatedAt(),
                items
        );
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
