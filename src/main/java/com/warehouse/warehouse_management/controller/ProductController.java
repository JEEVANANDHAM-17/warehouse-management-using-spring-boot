package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.dto.CreateProductRequest;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public Product create(@Valid @RequestBody CreateProductRequest request) {

        return productService.createProduct(request);
    }
}