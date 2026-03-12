package com.warehouse.warehouse_management.security;

import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.repository.UserRepository;
import com.warehouse.warehouse_management.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7).trim();

            try {

                String email = JwtUtil.extractEmail(token);

                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null) {
                    String roleName = user.getRole() != null && user.getRole().getName() != null
                            ? user.getRole().getName().trim().toUpperCase(Locale.ROOT)
                            : "";

                    if (roleName.startsWith("ROLE_")) {
                        roleName = roleName.substring(5);
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                writeUnauthorizedResponse(response, "Invalid token");
                return;
            }
        } else if (header != null && !header.isBlank()) {
            SecurityContextHolder.clearContext();
            writeUnauthorizedResponse(response, "Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ApiResponse<>(false, message, null));
    }
}
