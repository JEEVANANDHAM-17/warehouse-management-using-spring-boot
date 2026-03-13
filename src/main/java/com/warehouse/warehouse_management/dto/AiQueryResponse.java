package com.warehouse.warehouse_management.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AiQueryResponse(
        String answer,
        List<String> insights,
        List<String> suggestedActions,
        LocalDateTime generatedAt
) {
}
