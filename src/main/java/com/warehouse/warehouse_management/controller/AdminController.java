package com.warehouse.warehouse_management.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/create-admin")
    public String createAdmin() {
        return "Admin created successfully";
    }

}