package com.blognest.userservice.service;

import com.blognest.common.dto.ApiResponse;
import com.blognest.userservice.config.JwtConfig;
import com.blognest.userservice.dto.AuthRequest;
import com.blognest.userservice.dto.AuthResponse;
import com.blognest.userservice.dto.UserRegistrationRequest;
import com.blognest.userservice.dto.UserUpdateRequest;
import com.blognest.userservice.entity.User;
import com.blognest.userservice.exception.UserAlreadyExistsException;
import com.blognest.userservice.exception.UserNotFoundException;
import com.blognest.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;

    public ApiResponse<AuthResponse> authenticate(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtConfig.generateToken(authRequest.getUsername());

            User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            AuthResponse authResponse = AuthResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();

            return ApiResponse.success("Authentication successful", authResponse);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", authRequest.getUsername(), e);
            return ApiResponse.error("Invalid username or password");
        }
    }

    public ApiResponse<User> registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .role(User.Role.USER)
            .enabled(true)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        return ApiResponse.success("User registered successfully", savedUser);
    }

    public ApiResponse<User> getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return ApiResponse.success(user);
    }

    public ApiResponse<User> getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return ApiResponse.success(user);
    }

    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ApiResponse.success(users);
    }

    public ApiResponse<User> updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getUsername());
        
        return ApiResponse.success("User updated successfully", updatedUser);
    }

    public ApiResponse<Void> deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
        
        return ApiResponse.success("User deleted successfully", null);
    }

    public ApiResponse<List<User>> searchUsers(String keyword) {
        List<User> users = userRepository.findByKeyword(keyword);
        return ApiResponse.success(users);
    }

    public ApiResponse<List<User>> getUsersByRole(User.Role role) {
        List<User> users = userRepository.findByRole(role);
        return ApiResponse.success(users);
    }

    public ApiResponse<Long> getUsersCountByRole(User.Role role) {
        long count = userRepository.countByRole(role);
        return ApiResponse.success(count);
    }

    public ApiResponse<List<User>> getActiveUsers() {
        List<User> users = userRepository.findByEnabled(true);
        return ApiResponse.success(users);
    }

    public ApiResponse<List<User>> getUsersCreatedAfter(LocalDateTime startDate) {
        List<User> users = userRepository.findUsersCreatedAfter(startDate);
        return ApiResponse.success(users);
    }

    public boolean validateToken(String token, String username) {
        return jwtConfig.validateToken(token, username);
    }

    public String getUsernameFromToken(String token) {
        return jwtConfig.extractUsername(token);
    }
} 