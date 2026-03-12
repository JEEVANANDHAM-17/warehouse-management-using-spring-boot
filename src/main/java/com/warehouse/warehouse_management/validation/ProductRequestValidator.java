package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.dto.CreateProductRequest;
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

        if (request.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        if (productPersistenceService.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }
    }
}
