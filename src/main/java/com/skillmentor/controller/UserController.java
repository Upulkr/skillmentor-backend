package com.skillmentor.controller;

import com.skillmentor.dto.request.SyncUserRequest;
import com.skillmentor.dto.response.UserResponse;
import com.skillmentor.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * SYNC USER: Call this after frontend sign-in.
     * 
     * @param request:         Contains Clerk ID, Email, and optionally Role
     * @param clerkIdFromAuth: Injected by Spring Security from JWT
     */
    @PostMapping("/sync")
    public ResponseEntity<UserResponse> syncUser(
            @RequestBody @Valid SyncUserRequest request,
            @AuthenticationPrincipal String clerkIdFromAuth) {

        // Safety: Only sync the user that is authenticated
        if (!request.getClerkId().equals(clerkIdFromAuth)) {
            return ResponseEntity.status(403).build();
        }

        UserResponse syncedUser = userService.syncUser(request);
        return ResponseEntity.ok(syncedUser);
    }

    /**
     * GET MY PROFILE
     */
    // Add logic here if needed for front-end to get their own user data
}
