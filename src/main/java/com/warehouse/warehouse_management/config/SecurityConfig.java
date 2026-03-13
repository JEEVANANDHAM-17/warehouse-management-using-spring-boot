package com.warehouse.warehouse_management.config;

import com.warehouse.warehouse_management.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import com.warehouse.warehouse_management.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").authenticated()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/test").authenticated()
                        .requestMatchers(HttpMethod.GET, "/dashboard/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/products", "/products/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/products").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/products/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/inventory", "/inventory/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/inventory/add-stock").authenticated()
                        .requestMatchers(HttpMethod.POST, "/inventory/remove-stock").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/inventory/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/warehouses", "/warehouses/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/warehouses").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/warehouses/**").authenticated()
                        .requestMatchers("/orders", "/orders/**").authenticated()
                        .requestMatchers("/ai", "/ai/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/admin/create-admin").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            objectMapper.writeValue(
                                    response.getWriter(),
                                    new ApiResponse<>(false, "Unauthorized", null)
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            objectMapper.writeValue(
                                    response.getWriter(),
                                    new ApiResponse<>(false, "Forbidden", null)
                            );
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
