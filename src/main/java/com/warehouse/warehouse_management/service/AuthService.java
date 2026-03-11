package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.LoginRequest;
import com.warehouse.warehouse_management.dto.RegisterRequest;
import com.warehouse.warehouse_management.entity.Role;
import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.repository.RoleRepository;
import com.warehouse.warehouse_management.repository.UserRepository;
import com.warehouse.warehouse_management.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(RegisterRequest request) {
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        return "User registered successfully";
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return JwtUtil.generateToken(user.getEmail());
    }
}
