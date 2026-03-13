package com.warehouse.warehouse_management.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String WAREHOUSE_EVENTS_EXCHANGE = "warehouse.events";
    public static final String ORDER_CREATED_QUEUE = "warehouse.order.created";
    public static final String INVENTORY_UPDATED_QUEUE = "warehouse.inventory.updated";
    public static final String LOW_STOCK_QUEUE = "warehouse.inventory.low-stock";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String INVENTORY_UPDATED_ROUTING_KEY = "inventory.updated";
    public static final String LOW_STOCK_ROUTING_KEY = "inventory.low-stock";

    @Bean
    public TopicExchange warehouseEventsExchange() {
        return new TopicExchange(WAREHOUSE_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Queue inventoryUpdatedQueue() {
        return new Queue(INVENTORY_UPDATED_QUEUE, true);
    }

    @Bean
    public Queue lowStockQueue() {
        return new Queue(LOW_STOCK_QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange warehouseEventsExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(warehouseEventsExchange)
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding inventoryUpdatedBinding(Queue inventoryUpdatedQueue, TopicExchange warehouseEventsExchange) {
        return BindingBuilder.bind(inventoryUpdatedQueue)
                .to(warehouseEventsExchange)
                .with(INVENTORY_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding lowStockBinding(Queue lowStockQueue, TopicExchange warehouseEventsExchange) {
        return BindingBuilder.bind(lowStockQueue)
                .to(warehouseEventsExchange)
                .with(LOW_STOCK_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter rabbitMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        return rabbitTemplate;
    }
}
