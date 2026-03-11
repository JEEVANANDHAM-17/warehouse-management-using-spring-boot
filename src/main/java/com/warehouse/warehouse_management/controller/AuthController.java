package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.dto.LoginRequest;
import com.warehouse.warehouse_management.dto.RegisterRequest;
import com.warehouse.warehouse_management.response.ApiResponse;
import com.warehouse.warehouse_management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {

        String message = authService.register(request);

        return ResponseEntity.ok(new ApiResponse(true, message, null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {

        String token = authService.login(request);

        return ResponseEntity.ok(new ApiResponse(true, "Login successful", token));
    }
}