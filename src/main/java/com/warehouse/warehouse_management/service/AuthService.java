package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.LoginRequest;
import com.warehouse.warehouse_management.dto.RegisterRequest;
import com.warehouse.warehouse_management.entity.Role;
import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.persistence.RolePersistenceService;
import com.warehouse.warehouse_management.persistence.UserPersistenceService;
import com.warehouse.warehouse_management.security.JwtUtil;
import com.warehouse.warehouse_management.validation.AuthRequestValidator;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserPersistenceService userPersistenceService;
    private final RolePersistenceService rolePersistenceService;
    private final PasswordEncoder passwordEncoder;
    private final AuthRequestValidator authRequestValidator;

    public AuthService(UserPersistenceService userPersistenceService,
                       RolePersistenceService rolePersistenceService,
                       PasswordEncoder passwordEncoder,
                       AuthRequestValidator authRequestValidator) {
        this.userPersistenceService = userPersistenceService;
        this.rolePersistenceService = rolePersistenceService;
        this.passwordEncoder = passwordEncoder;
        this.authRequestValidator = authRequestValidator;
    }

    public String register(RegisterRequest request) {
        authRequestValidator.validateRegister(request);

        Role role = rolePersistenceService.getRequiredByName(request.getRole());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userPersistenceService.save(user);

        return "User registered successfully";
    }

    public String login(LoginRequest request) {
        User user = userPersistenceService.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return JwtUtil.generateToken(user.getEmail());
    }
}
