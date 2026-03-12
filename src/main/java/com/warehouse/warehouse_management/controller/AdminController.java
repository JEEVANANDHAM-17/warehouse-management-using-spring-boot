package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.dto.RegisterRequest;
import com.warehouse.warehouse_management.response.ApiResponse;
import com.warehouse.warehouse_management.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/create-admin")
    public ApiResponse createAdmin(@Valid @RequestBody RegisterRequest request) {

        adminService.createAdmin(request);

        return new ApiResponse(true, "Admin created successfully", null);
    }
}