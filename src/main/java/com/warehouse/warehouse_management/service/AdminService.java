package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.dto.CreateAdminRequest;
import com.warehouse.warehouse_management.entity.Role;
import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.repository.RoleRepository;
import com.warehouse.warehouse_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public void createAdmin(CreateAdminRequest request) {

        Role role = roleRepository.findByName("ADMIN")
                .orElseThrow();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
    }
}
