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

    private static final int DEFAULT_REORDER_LEVEL = 5;

    private final ProductPersistenceService productPersistenceService;
    private final ProductRequestValidator productRequestValidator;

    public Product createProduct(CreateProductRequest request) {
        productRequestValidator.validateCreateProduct(request);

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .reorderLevel(resolveReorderLevel(request.getReorderLevel(), DEFAULT_REORDER_LEVEL))
                .build();

        return productPersistenceService.save(product);
    }

    public List<Product> getAllProducts(String name, String sku) {
        productRequestValidator.validateReadAccess();
        return productPersistenceService.search(normalize(name), normalize(sku));
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
        product.setReorderLevel(resolveReorderLevel(request.getReorderLevel(), product.getReorderLevel()));

        return productPersistenceService.save(product);
    }

    private Integer resolveReorderLevel(Integer requestedValue, Integer fallbackValue) {
        if (requestedValue != null) {
            return requestedValue;
        }

        if (fallbackValue != null && fallbackValue > 0) {
            return fallbackValue;
        }

        return DEFAULT_REORDER_LEVEL;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
