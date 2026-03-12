package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.CreateProductRequest;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(CreateProductRequest request) {

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();

        return productRepository.save(product);
    }
}