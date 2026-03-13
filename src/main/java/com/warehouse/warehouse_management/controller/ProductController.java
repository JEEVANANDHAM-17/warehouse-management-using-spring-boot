package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.dto.CreateProductRequest;
import com.warehouse.warehouse_management.dto.PageResponse;
import com.warehouse.warehouse_management.entity.Product;
import com.warehouse.warehouse_management.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public Product create(@Valid @RequestBody CreateProductRequest request) {

        return productService.createProduct(request);
    }

    @GetMapping
    public PageResponse<Product> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.getAllProducts(name, sku, page, size);
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody CreateProductRequest request) {
        return productService.updateProduct(id, request);
    }
}
