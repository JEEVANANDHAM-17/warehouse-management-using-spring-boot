package com.warehouse.warehouse_management.controller;

import com.warehouse.warehouse_management.dto.AiQueryRequest;
import com.warehouse.warehouse_management.dto.AiQueryResponse;
import com.warehouse.warehouse_management.service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/query")
    public AiQueryResponse query(@Valid @RequestBody AiQueryRequest request) {
        return aiAssistantService.answer(request.query());
    }
}
