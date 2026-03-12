package com.warehouse.warehouse_management.persistence;

import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductPersistenceService {

    private final ProductRepository productRepository;

    public boolean existsBySku(String sku) {
        return productRepository.findBySku(sku).isPresent();
    }

    public Product getRequiredById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }
}
