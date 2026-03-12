package com.warehouse.warehouse_management.validation;

import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.persistence.UserPersistenceService;
import com.warehouse.warehouse_management.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthenticatedRequestValidator {

    private final UserPersistenceService userPersistenceService;

    public User requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Unauthorized");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof String token) || token.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Unauthorized");
        }

        try {
            String email = JwtUtil.extractEmail(token);

            return userPersistenceService.findByEmail(email)
                    .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Unauthorized"));
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("Unauthorized");
        }
    }

    public User requireRole(String... allowedRoles) {
        User user = requireUser();
        String currentRole = normalizeRole(user.getRole() != null ? user.getRole().getName() : null);
        Set<String> requiredRoles = Arrays.stream(allowedRoles)
                .map(this::normalizeRole)
                .collect(Collectors.toSet());

        if (!requiredRoles.contains(currentRole)) {
            throw new AccessDeniedException("Forbidden");
        }

        return user;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }

        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);

        if (normalizedRole.startsWith("ROLE_")) {
            return normalizedRole.substring(5);
        }

        return normalizedRole;
    }
}
