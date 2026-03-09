package com.skillmentor.controller;

import com.skillmentor.dto.response.DashboardStatsResponse;
import com.skillmentor.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final com.skillmentor.service.UserService userService;

    @GetMapping("/stats")
    public DashboardStatsResponse getDashboardStats() {
        return adminService.getStats();
    }

    /**
     * MENTOR CRUD is handled in MentorController
     * with @PreAuthorize("hasRole('ADMIN')")
     * But we can also manage general USERS (Students) here.
     */

    @GetMapping("/users")
    public java.util.List<com.skillmentor.dto.response.UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public com.skillmentor.dto.response.UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/users/{id}")
    public com.skillmentor.dto.response.UserResponse updateUser(@PathVariable Long id,
            @RequestBody com.skillmentor.dto.request.UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    public org.springframework.http.ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
