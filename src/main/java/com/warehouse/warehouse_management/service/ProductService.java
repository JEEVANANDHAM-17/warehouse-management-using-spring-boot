package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.CreateProductRequest;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.persistence.ProductPersistenceService;
import com.warehouse.warehouse_management.validation.ProductRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductPersistenceService productPersistenceService;
    private final ProductRequestValidator productRequestValidator;

    public Product createProduct(CreateProductRequest request) {
        productRequestValidator.validateCreateProduct(request);

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();

        return productPersistenceService.save(product);
    }

    public List<Product> getAllProducts() {
        productRequestValidator.validateReadAccess();
        return productPersistenceService.findAll();
    }

    public Product getProduct(Long productId) {
        productRequestValidator.validateReadAccess();
        return productPersistenceService.getRequiredById(productId);
    }

    public Product updateProduct(Long productId, CreateProductRequest request) {
        Product product = productRequestValidator.validateUpdateProduct(productId, request);

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        return productPersistenceService.save(product);
    }
}
