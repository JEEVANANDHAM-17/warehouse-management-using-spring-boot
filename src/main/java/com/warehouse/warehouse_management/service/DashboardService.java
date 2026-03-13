package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.DashboardSummaryResponse;
import com.warehouse.warehouse_management.validation.AuthenticatedRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AuthenticatedRequestValidator authenticatedRequestValidator;
    private final CachedReadService cachedReadService;

    public DashboardSummaryResponse getSummary() {
        authenticatedRequestValidator.requireUser();
        return cachedReadService.getDashboardSummary();
    }
}
