package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.dto.CreateAdminRequest;
import com.warehouse.warehouse_management.response.ApiResponse;
import com.warehouse.warehouse_management.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/create-admin")
    public ApiResponse<String> createAdmin(@Valid @RequestBody CreateAdminRequest request) {

        adminService.createAdmin(request);

        return new ApiResponse<>(true, "Admin created successfully", null);
    }
}
