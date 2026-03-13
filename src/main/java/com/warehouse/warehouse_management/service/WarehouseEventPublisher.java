package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.config.RabbitMqConfig;
import com.warehouse.warehouse_management.event.InventoryUpdatedEvent;
import com.warehouse.warehouse_management.event.LowStockDetectedEvent;
import com.warehouse.warehouse_management.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        publish(RabbitMqConfig.ORDER_CREATED_ROUTING_KEY, event);
    }

    public void publishInventoryUpdated(InventoryUpdatedEvent event) {
        publish(RabbitMqConfig.INVENTORY_UPDATED_ROUTING_KEY, event);
    }

    public void publishLowStockDetected(LowStockDetectedEvent event) {
        publish(RabbitMqConfig.LOW_STOCK_ROUTING_KEY, event);
    }

    private void publish(String routingKey, Object payload) {
        Runnable publisher = () -> {
            try {
                rabbitTemplate.convertAndSend(RabbitMqConfig.WAREHOUSE_EVENTS_EXCHANGE, routingKey, payload);
            } catch (AmqpException exception) {
                log.warn("RabbitMQ publish failed for routing key {}", routingKey, exception);
            }
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.run();
                }
            });
            return;
        }

        publisher.run();
    }
}
