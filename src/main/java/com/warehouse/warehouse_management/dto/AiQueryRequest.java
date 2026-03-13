package com.warehouse.warehouse_management.dto;

import jakarta.validation.constraints.NotBlank;

public record AiQueryRequest(
        @NotBlank(message = "Query is required")
        String query
) {
}
