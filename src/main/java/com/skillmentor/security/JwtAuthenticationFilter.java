package com.skillmentor.security;

import com.skillmentor.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT AUTHENTICATION FILTER
 * 
 * This filter runs ONCE per request.
 * It checks for a Bearer token, validates it, and tells Spring "who" the user
 * is.
 */
@Component
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Get the "Authorization" header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId;

        // 2. Validate format: Must start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract token
        jwt = authHeader.substring(7);

        try {
            // 4. Validate and get User ID
            userId = jwtProvider.getUserIdFromToken(jwt);
            String role = jwtProvider.getRoleFromToken(jwt); // e.g. "admin" or "student"

            // 4.1 Fallback: Check local database if role is STUDENT (default)
            // This handles cases where Clerk JWT hasn't been updated with roles yet
            if ("STUDENT".equalsIgnoreCase(role)) {
                String dbRole = userRepository.findByClerkId(userId)
                        .map(user -> user.getRole().name())
                        .orElse("STUDENT");
                log.info("JWT fallback: DB Role for user {} is {}", userId, dbRole);
                role = dbRole;
            }

            // 5. If valid and not already authenticated in this context
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Convert Clerk role to Spring Security Authority
                // Prefix with "ROLE_" is a standard Spring Security convention
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Set user in Security Context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            log.error("JWT Validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
