package com.warehouse.warehouse_management.repository;

import com.warehouse.warehouse_management.entity.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = {"warehouse", "createdBy"})
    List<CustomerOrder> findAllByOrderByCreatedAtDescIdDesc();

    @EntityGraph(attributePaths = {"warehouse", "createdBy", "items", "items.product"})
    @Query("select o from CustomerOrder o where o.id = :id")
    Optional<CustomerOrder> findDetailedById(@Param("id") Long id);
}
