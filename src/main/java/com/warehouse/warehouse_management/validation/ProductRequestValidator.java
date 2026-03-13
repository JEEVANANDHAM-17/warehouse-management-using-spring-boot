package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.dto.CreateProductRequest;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.persistence.ProductPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductRequestValidator {

    private final AuthenticatedRequestValidator authenticatedRequestValidator;
    private final ProductPersistenceService productPersistenceService;

    public void validateCreateProduct(CreateProductRequest request) {
        authenticatedRequestValidator.requireRole("ADMIN", "SUPER_ADMIN");
        validatePayload(request);

        if (productPersistenceService.findBySku(request.getSku()).isPresent()) {
            throw new IllegalArgumentException("SKU already exists");
        }
    }

    public void validateReadAccess() {
        authenticatedRequestValidator.requireUser();
    }

    public Product validateUpdateProduct(Long productId, CreateProductRequest request) {
        authenticatedRequestValidator.requireRole("ADMIN", "SUPER_ADMIN");
        validatePayload(request);

        Product product = productPersistenceService.getRequiredById(productId);

        productPersistenceService.findBySku(request.getSku())
                .filter(existingProduct -> !existingProduct.getId().equals(productId))
                .ifPresent(existingProduct -> {
                    throw new IllegalArgumentException("SKU already exists");
                });

        return product;
    }

    private void validatePayload(CreateProductRequest request) {
        if (request.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        if (request.getReorderLevel() != null && request.getReorderLevel() <= 0) {
            throw new IllegalArgumentException("Reorder level must be greater than 0");
        }
    }
}
