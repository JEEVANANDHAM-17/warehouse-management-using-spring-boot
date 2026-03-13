package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.CreateProductRequest;
import com.warehouse.warehouse_management.dto.PageResponse;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.persistence.ProductPersistenceService;
import com.warehouse.warehouse_management.validation.AuthenticatedRequestValidator;
import com.warehouse.warehouse_management.validation.ProductRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int DEFAULT_REORDER_LEVEL = 5;

    private final ProductPersistenceService productPersistenceService;
    private final ProductRequestValidator productRequestValidator;
    private final AuthenticatedRequestValidator authenticatedRequestValidator;
    private final CachedReadService cachedReadService;
    private final AuditLogService auditLogService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCTS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOW_STOCK, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    })
    public Product createProduct(CreateProductRequest request) {
        productRequestValidator.validateCreateProduct(request);
        User actor = authenticatedRequestValidator.requireUser();

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .reorderLevel(resolveReorderLevel(request.getReorderLevel(), DEFAULT_REORDER_LEVEL))
                .build();

        Product savedProduct = productPersistenceService.save(product);
        auditLogService.log(
                "PRODUCT_CREATED",
                "PRODUCT",
                savedProduct.getId(),
                "Created product " + savedProduct.getName() + " with SKU " + savedProduct.getSku(),
                actor
        );
        return savedProduct;
    }

    public PageResponse<Product> getAllProducts(String name, String sku, int page, int size) {
        productRequestValidator.validateReadAccess();
        validatePageRequest(page, size);
        return cachedReadService.getProducts(normalize(name), normalize(sku), page, size);
    }

    public Product getProduct(Long productId) {
        productRequestValidator.validateReadAccess();
        return productPersistenceService.getRequiredById(productId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCTS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOW_STOCK, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    })
    public Product updateProduct(Long productId, CreateProductRequest request) {
        Product product = productRequestValidator.validateUpdateProduct(productId, request);
        User actor = authenticatedRequestValidator.requireUser();

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setReorderLevel(resolveReorderLevel(request.getReorderLevel(), product.getReorderLevel()));

        Product savedProduct = productPersistenceService.save(product);
        auditLogService.log(
                "PRODUCT_UPDATED",
                "PRODUCT",
                savedProduct.getId(),
                "Updated product " + savedProduct.getName() + " with SKU " + savedProduct.getSku(),
                actor
        );
        return savedProduct;
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

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to 0");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
    }
}
