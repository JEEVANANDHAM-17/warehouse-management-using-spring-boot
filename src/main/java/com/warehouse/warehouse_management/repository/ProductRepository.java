package com.warehouse.warehouse_management.repository;

import com.warehouse.warehouse_management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Query("""
            select p
            from Product p
            where (:name is null or lower(p.name) like lower(concat('%', :name, '%')))
              and (:sku is null or lower(p.sku) = lower(:sku))
            order by p.name asc, p.id asc
            """)
    List<Product> search(@Param("name") String name, @Param("sku") String sku);
}
