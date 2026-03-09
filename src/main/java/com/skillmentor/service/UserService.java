package com.skillmentor.service;

import com.skillmentor.dto.request.SyncUserRequest;
import com.skillmentor.dto.response.UserResponse;
import com.skillmentor.entity.User;
import com.skillmentor.entity.UserRole;
import com.skillmentor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse syncUser(SyncUserRequest request) {
        log.info("SYNC ATTEMPT | ClerkId: {} | Email: {} | Role: {}",
                request.getClerkId(), request.getEmail(), request.getRole());

        // 1. Try to find user by clerkId
        User user = userRepository.findByClerkId(request.getClerkId())
                .map(existingUser -> {
                    log.info("User found by clerkId. Updating details.");
                    existingUser.setEmail(request.getEmail());
                    updateBaseDetails(existingUser, request);
                    updateRoleSafely(existingUser, request.getRole());

                    // Fix: Force upgrade to ADMIN if the email is a designated admin account
                    if (request.getEmail().equalsIgnoreCase("admin@skillmentor.com") ||
                            request.getEmail().equalsIgnoreCase("admin@admin.com")) {
                        if (existingUser.getRole() != UserRole.ADMIN) {
                            log.info("FORCING UPGRADE to ADMIN for email: {}. Current role was: {}",
                                    request.getEmail(), existingUser.getRole());
                            existingUser.setRole(UserRole.ADMIN);
                        }
                    }

                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    // 2. Not found by clerkId, search by email to link existing records
                    return userRepository.findByEmailIgnoreCase(request.getEmail())
                            .map(existingByEmail -> {
                                log.info("User found by email ({}). Linking to clerkId: {}", request.getEmail(),
                                        request.getClerkId());
                                existingByEmail.setClerkId(request.getClerkId());
                                updateBaseDetails(existingByEmail, request);
                                updateRoleSafely(existingByEmail, request.getRole());
                                return userRepository.save(existingByEmail);
                            })
                            .orElseGet(() -> {
                                // 3. Truly new user
                                log.info("New User. Creating entry in DB for clerkId: {}", request.getClerkId());
                                UserRole role = UserRole.STUDENT;
                                if (request.getRole() != null) {
                                    try {
                                        role = UserRole.valueOf(request.getRole().toUpperCase());
                                    } catch (IllegalArgumentException e) {
                                        log.warn("Invalid role received. Defaulting to STUDENT.");
                                    }
                                }

                                User newUser = User.builder()
                                        .clerkId(request.getClerkId())
                                        .email(request.getEmail())
                                        .firstName(request.getFirstName())
                                        .lastName(request.getLastName())
                                        .profileImageUrl(request.getProfileImageUrl())
                                        .role(role)
                                        .build();

                                return userRepository.save(newUser);
                            });
                });

        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public java.util.List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new com.skillmentor.exception.ResourceNotFoundException("User not found with id: " + id));
        return convertToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, com.skillmentor.dto.request.UpdateUserRequest request) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new com.skillmentor.exception.ResourceNotFoundException("User not found with id: " + id));

        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getProfileImageUrl() != null)
            user.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getRole() != null) {
            try {
                user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role: {}. Keeping existing.", request.getRole());
            }
        }

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        if (!userRepository.existsById(id)) {
            throw new com.skillmentor.exception.ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private void updateBaseDetails(User user, SyncUserRequest request) {
        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getProfileImageUrl() != null)
            user.setProfileImageUrl(request.getProfileImageUrl());
    }

    private void updateRoleSafely(User user, String newRoleStr) {
        if (newRoleStr == null)
            return;

        try {
            UserRole newRole = UserRole.valueOf(newRoleStr.toUpperCase());
            // PROTECT: Don't downgrade from ADMIN if the incoming role is STUDENT (likely a
            // default)
            if (user.getRole() == UserRole.ADMIN && newRole == UserRole.STUDENT) {
                log.info("Skipping role sync for ADMIN user (clerkId: {}) to prevent downgrade to STUDENT.",
                        user.getClerkId());
                return;
            }
            user.setRole(newRole);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role received: {}. Keeping existing.", newRoleStr);
        }
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .clerkId(user.getClerkId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole().name())
                .build();
    }
}
