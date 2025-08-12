package com.blognest.userservice.controller;

import com.blognest.common.dto.ApiResponse;
import com.blognest.userservice.dto.AuthRequest;
import com.blognest.userservice.dto.AuthResponse;
import com.blognest.userservice.dto.UserRegistrationRequest;
import com.blognest.userservice.dto.UserUpdateRequest;
import com.blognest.userservice.entity.User;
import com.blognest.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User authentication and management APIs")
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/login")
    @Operation(summary = "Authenticate user", description = "Login with username and password")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticate(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Authentication attempt for user: {}", authRequest.getUsername());
        ApiResponse<AuthResponse> response = userService.authenticate(authRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<ApiResponse<User>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("User registration attempt for username: {}", request.getUsername());
        ApiResponse<User> response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by ID")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        ApiResponse<User> response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve user information by username")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public ResponseEntity<ApiResponse<User>> getUserByUsername(@PathVariable String username) {
        log.info("Fetching user with username: {}", username);
        ApiResponse<User> response = userService.getUserByUsername(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.info("Fetching all users");
        ApiResponse<List<User>> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user information")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, 
                                                      @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);
        ApiResponse<User> response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete user account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        ApiResponse<Void> response = userService.deleteUser(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by keyword")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(@RequestParam String keyword) {
        log.info("Searching users with keyword: {}", keyword);
        ApiResponse<List<User>> response = userService.searchUsers(keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Retrieve users by role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable User.Role role) {
        log.info("Fetching users with role: {}", role);
        ApiResponse<List<User>> response = userService.getUsersByRole(role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}/count")
    @Operation(summary = "Get user count by role", description = "Get count of users by role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getUsersCountByRole(@PathVariable User.Role role) {
        log.info("Fetching user count for role: {}", role);
        ApiResponse<Long> response = userService.getUsersCountByRole(role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active users", description = "Retrieve all active users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getActiveUsers() {
        log.info("Fetching active users");
        ApiResponse<List<User>> response = userService.getActiveUsers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/created-after")
    @Operation(summary = "Get users created after date", description = "Retrieve users created after specified date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersCreatedAfter(@RequestParam LocalDateTime startDate) {
        log.info("Fetching users created after: {}", startDate);
        ApiResponse<List<User>> response = userService.getUsersCreatedAfter(startDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/validate")
    @Operation(summary = "Validate token", description = "Validate JWT token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String token) {
        log.info("Validating JWT token");
        String cleanToken = token.replace("Bearer ", "");
        String username = userService.getUsernameFromToken(cleanToken);
        boolean isValid = userService.validateToken(cleanToken, username);
        return ResponseEntity.ok(ApiResponse.success("Token validation completed", isValid));
    }

    @GetMapping("/auth/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@RequestHeader("Authorization") String token) {
        log.info("Fetching current user information");
        String username = userService.getUsernameFromToken(token.replace("Bearer ", ""));
        ApiResponse<User> response = userService.getUserByUsername(username);
        return ResponseEntity.ok(response);
    }
} 