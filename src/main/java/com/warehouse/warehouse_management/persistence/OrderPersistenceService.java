package com.warehouse.warehouse_management.persistence;

import com.warehouse.warehouse_management.entity.CustomerOrder;
import com.warehouse.warehouse_management.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderPersistenceService {

    private final OrderRepository orderRepository;

    public CustomerOrder save(CustomerOrder order) {
        return orderRepository.save(order);
    }

    public List<CustomerOrder> findAllSummaries() {
        return orderRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    public CustomerOrder getRequiredDetailedById(Long orderId) {
        return orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public long countAll() {
        return orderRepository.count();
    }
}
