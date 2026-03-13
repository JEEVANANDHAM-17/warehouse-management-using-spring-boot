package com.warehouse.warehouse_management.config;

import com.warehouse.warehouse_management.entity.Role;
import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.repository.ProductRepository;
import com.warehouse.warehouse_management.repository.RoleRepository;
import com.warehouse.warehouse_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createRoleIfNotExists("SUPER_ADMIN");
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("MANAGER");
        createRoleIfNotExists("STAFF");

        createDefaultSuperAdmin();
        assignDefaultReorderLevels();
    }

    private void createRoleIfNotExists(String roleName) {

        roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(roleName)
                                .build()
                ));
    }

    private void createDefaultSuperAdmin() {

        if (userRepository.findByEmail("superadmin@warehouse.com").isEmpty()) {

            Role role = roleRepository.findByName("SUPER_ADMIN").orElseThrow();

            User user = User.builder()
                    .name("Super Admin")
                    .email("superadmin@warehouse.com")
                    .password(passwordEncoder.encode("super123"))
                    .role(role)
                    .build();

            userRepository.save(user);
        }
    }

    private void assignDefaultReorderLevels() {
        productRepository.findAll().stream()
                .filter(product -> product.getReorderLevel() == null || product.getReorderLevel() <= 0)
                .forEach(product -> {
                    product.setReorderLevel(5);
                    productRepository.save(product);
                });
    }
}
